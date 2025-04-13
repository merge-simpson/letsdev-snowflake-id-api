package me.letsdev.persistence.id.snowflake;

public final class SnowflakeConstants {
    public static final long NETTEE_EPOCH = 1_743_000_000_000L;

    private SnowflakeConstants() {}

    public static final class SnowflakeDefault {
        public static final int WORKER_ID_BIT_SIZE = 5;
        public static final int DATACENTER_ID_BIT_SIZE = 5;
        public static final int SEQUENCE_BIT_SIZE = 12;

        public static final int WORKER_ID_SHIFT = SEQUENCE_BIT_SIZE;
        public static final int DATACENTER_ID_SHIFT = SEQUENCE_BIT_SIZE + WORKER_ID_BIT_SIZE;
        public static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BIT_SIZE + WORKER_ID_BIT_SIZE + DATACENTER_ID_BIT_SIZE;

        public static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BIT_SIZE);

        public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BIT_SIZE);
        public static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BIT_SIZE);

        private SnowflakeDefault() {}
    }
}