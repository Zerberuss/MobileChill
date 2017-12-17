package net.sytes.schneider.mobilechill.database.Converter;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Timo Hasenbichler on 17.12.2017.
 */

public class Converters {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }
}
