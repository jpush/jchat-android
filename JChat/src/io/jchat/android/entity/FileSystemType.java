package io.jchat.android.entity;

/**
 * Created by caiyaoguan on 16/12/1.
 */

public enum FileSystemType {

    photo,
    music,
    video,
    text,
    other;


    public static FileSystemType getFileTypeByOrdinal(int ordinal) {

        for (FileSystemType type : values()) {

            if (type.ordinal() == ordinal) {

                return type;
            }

        }

        return photo;

    }
}
