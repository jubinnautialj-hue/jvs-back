package cn.bctools.gateway.utils;


import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;

/**
 * @author gj
 */
public class CodeKaptchaUtils {

    static DefaultKaptcha producer = new DefaultKaptcha();

    static {
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");
        properties.put("kaptcha.textproducer.font.color", "black");
        properties.put("kaptcha.textproducer.char.space", "5");
        //如果需要生成算法验证码加上一下配置
        properties.put("kaptcha.textproducer.char.string", "1234567890");
        //如果需要去掉干扰线
        Config config = new Config(properties);
        producer.setConfig(config);
    }


    static String[] a = {"+", "-", "*", "/"};

    @SneakyThrows
    public static int get(OutputStream outputStream) {
        String s1 = producer.createText().substring(0, 1);
        String s2 = producer.createText().substring(1, 2);
        String function = a[new Random().nextInt(a.length)];
        int c = 0;
        BufferedImage image = null;
        switch (function) {
            case "+":
                c = Integer.parseInt(s1) + Integer.parseInt(s2);
                image = producer.createImage(s1 + "+" + s2 + "=?");
                break;
            case "-":
                c = Integer.parseInt(s1) - Integer.parseInt(s2);
                image = producer.createImage(s1 + "-" + s2 + "=?");
                break;
            case "*":
                c = Integer.parseInt(s1) * Integer.parseInt(s2);
                image = producer.createImage(s1 + "x" + s2 + "=?");
                break;
            case "/":
                c = Integer.parseInt(s1) / Integer.parseInt(s2);
                image = producer.createImage(s1 + "/" + s2 + "=?");
                break;
        }
        System.out.println(s1 + "" + function + "" + s2 + " " + c);
        ImageIO.write(image, "jpg", outputStream);
        return c;
    }
}
