package net.library.exception;

import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

public final class MdcUtils {

    public static final String MDC_REQUEST_ID = "cid";
    private static final Logger LOGGER = LoggerFactory.getLogger(MdcUtils.class);

    private MdcUtils() {
    }

    public static String getOrMakeCid(Supplier<String> supplier) {
        final String cid = supplier.get();
        if (StringUtils.isBlank(cid)) {
            return UUID.randomUUID().toString();
        } else {
            return cid;
        }
    }

    /**
     * Init Mapped Diagnostic Context
     *
     * @return String
     */
    public static String initMdcCid() {
        final var id = UUID.randomUUID().toString();
        MDC.put(MDC_REQUEST_ID, id);
        LOGGER.trace("++++++++++++ REQUEST CID INITIALIZED +++++++++++++++++");
        return id;
    }


    public static void initMdcCid(final String cidRaw) {
        final var cid = getOrMakeCid(() -> cidRaw);
        MDC.put(MDC_REQUEST_ID, cid);
        LOGGER.trace("++++++++++++ REQUEST CID INITIALIZED +++++++++++++++++");
    }

    /**
     * Clear Mapped Diagnostic Context
     */
    public static void clearMdcCid() {
        LOGGER.trace("-------------REQUEST CID DESTROYED ------------");
        MDC.remove(MDC_REQUEST_ID);
    }

    public static String getCid() {
        return getOrMakeCid(() -> MDC.get(MDC_REQUEST_ID));
    }
}

