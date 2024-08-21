package net.library.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static net.library.exception.Constants.CORRELATION_ID_HEADER_NAME;
import static net.library.exception.MdcUtils.getOrMakeCid;

public class Utils {

    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

}
