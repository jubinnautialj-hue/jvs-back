package cn.bctools.report.model.univer.conf;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class UStyleN implements Serializable {

    private static final long serialVersionUID = 8048248876918302574L;

    private String pattern;
}
