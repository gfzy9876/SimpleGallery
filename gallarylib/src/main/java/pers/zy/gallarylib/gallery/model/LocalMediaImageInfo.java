package pers.zy.gallarylib.gallery.model;

import android.os.Parcel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * date: 2020/7/1   time: 12:01 PM
 * author zy
 * Have a nice day :)
 **/
public class LocalMediaImageInfo extends LocalMediaInfo {
    public LocalMediaImageInfo(@NotNull String realPath, @NotNull String contentUriPath, int mediaType, @NotNull String mimeType, long size, @NotNull String displayName, long width, long height, @Nullable String thumbnailPath, long duration) {
        super(realPath, contentUriPath, mediaType, mimeType, size, displayName, width, height, thumbnailPath, duration);
    }

    public LocalMediaImageInfo(@NotNull Parcel parcel) {
        super(parcel);
    }

    public static final Creator<LocalMediaImageInfo> CREATOR = new Creator<LocalMediaImageInfo>() {
        @Override
        public LocalMediaImageInfo createFromParcel(Parcel in) {
            return new LocalMediaImageInfo(in);
        }

        @Override
        public LocalMediaImageInfo[] newArray(int size) {
            return new LocalMediaImageInfo[size];
        }
    };
}
