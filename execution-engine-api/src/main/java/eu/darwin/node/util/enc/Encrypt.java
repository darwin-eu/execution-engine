package eu.darwin.node.util.enc;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class Encrypt implements AttributeConverter<String, String> {

    private final EncryptionUtil encryptionUtil;

    public Encrypt(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public String convertToDatabaseColumn(String s) {
        return encryptionUtil.encrypt(s);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return encryptionUtil.decrypt(s);
    }
}
