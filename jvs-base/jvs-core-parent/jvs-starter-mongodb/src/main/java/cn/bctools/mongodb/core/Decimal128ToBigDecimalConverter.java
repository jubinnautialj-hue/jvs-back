package cn.bctools.mongodb.core;

import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.math.BigDecimal;

/**
 * @author jvs The type Decimal 128 to big decimal converter.
 */
@ReadingConverter
@WritingConverter
public class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {

    public BigDecimal convert(Decimal128 decimal128) {
        return decimal128.bigDecimalValue();
    }

}
