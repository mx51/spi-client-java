package com.assemblypayments.spi;

import com.assemblypayments.spi.model.Secrets;

import java.lang.reflect.Field;

public class SpiClientTestUtils {

    public static Object getInstanceField(Object instance, String fieldName) throws IllegalAccessException, IllegalArgumentException {
        Class<?> secretClass = instance.getClass();
        Field fields[] = secretClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field.get(instance);
            }
        }

        return null;
    }

    public static void setInstanceField(Object instance, String fieldName, Object fieldValue) throws IllegalAccessException, IllegalArgumentException {
        Class<?> secretClass = instance.getClass();
        Field fields[] = secretClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(instance, fieldValue);
                return;
            }
        }
    }

    public static Secrets setTestSecrets(String encKey, String hmacKey) {
        if (encKey == null & hmacKey == null) {
            encKey = "81CF9E6A14CDAF244A30B298D4CECB505C730CE352C6AF6E1DE61B3232E24D3F";
            hmacKey = "D35060723C9EECDB8AEA019581381CB08F64469FC61A5A04FE553EBDB5CD55B9";
        }

        return new Secrets(encKey, hmacKey);
    }
}
