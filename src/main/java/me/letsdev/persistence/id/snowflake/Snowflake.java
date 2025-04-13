package me.letsdev.persistence.id.snowflake;

import me.letsdev.MillisecondSupplier;
import me.letsdev.SystemMilliseconds;

import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.NETTEE_EPOCH;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.DATACENTER_ID_SHIFT;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.MAX_DATACENTER_ID;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.MAX_WORKER_ID;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.SEQUENCE_MASK;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.TIMESTAMP_LEFT_SHIFT;
import static me.letsdev.persistence.id.snowflake.SnowflakeConstants.SnowflakeDefault.WORKER_ID_SHIFT;

public final class Snowflake {

    private final MillisecondSupplier milliseconds;

    private final long datacenterId;
    private final long workerId;
    private final long epoch;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public Snowflake(SnowflakeProperties properties) {
        // 각 반환이 non-null임이 로직상으로 보장되었습니다. 원하면 더 방어적인 코드도 좋아요.
        this(properties.datacenterId(), properties.workerId(), properties.epoch());
    }

    public Snowflake(long datacenterId, long workerId, long epoch) {
        this(new SystemMilliseconds(), datacenterId, workerId, epoch);
    }

    public Snowflake(MillisecondSupplier milliseconds, long datacenterId, long workerId, long epoch) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0", MAX_WORKER_ID
            ));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format(
                    "datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID
            ));
        }

        this.milliseconds = milliseconds;
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.epoch = epoch >= 0 ? epoch : NETTEE_EPOCH;
    }

    public synchronized long nextId() {
        long timestamp = milliseconds.getAsLong();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp
            ));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return ((timestamp - epoch) << TIMESTAMP_LEFT_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        // busy-wait: 가상스레드 환경에서 특히 안 좋지만, 이 로직에서는 다음 이유로 사용합니다.
        //  (1) 실제 대기 시간이 굉장히 드물고 짧은 순간 발생
        //  (2) 점유·해제를 오가는 동안 Context switching 비용을 고려
        //  (3) 점유를 회피하여 개선을 기대하는 것보다(개선될지 아닐지도 모르지만)
        //      다음 밀리초 때 빠르게 아이디를 제공하는 게 오히려
        //      신속한 아이디 제공으로 병목 시간 감소할 가능성 높음.
        //
        //  이상 이유로:
        //    LockSupport.parkNanos(), Thread.sleep() 등을 사용하는 것보다
        //    busy wait로 짧은 시간 CPU를 점유하는 게 나을 수 있습니다.
        long timestamp = milliseconds.getAsLong();
        while (timestamp <= lastTimestamp) {
            timestamp = milliseconds.getAsLong();
        }
        return timestamp;
    }
}