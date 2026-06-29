package cn.bctools.wps.utils;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class Token {

    private int expiresIn;
    private String token;
    private String wpsUrl;

}
