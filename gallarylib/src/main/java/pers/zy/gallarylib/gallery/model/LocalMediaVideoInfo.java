package pers.zy.gallarylib.gallery.model;

import android.os.Parcel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * date: 2020/7/1   time: 12:03 PM
 * author zy
 * Have a nice day :)
 **/
public class LocalMediaVideoInfo extends LocalMediaInfo {
    public LocalMediaVideoInfo(@NotNull String realPath, @NotNull String contentUriPath, int mediaType, @NotNull String mimeType, long size, @NotNull String displayName, long width, long height, @Nullable String thumbnailPath, long duration) {
        super(realPath, contentUriPath, mediaType, mimeType, size, displayName, width, height, thumbnailPath, duration);
    }

    public LocalMediaVideoInfo(@NotNull Parcel parcel) {
        super(parcel);
    }

    public static final Creator<LocalMediaVideoInfo> CREATOR = new Creator<LocalMediaVideoInfo>() {
        @Override
        public LocalMediaVideoInfo createFromParcel(Parcel in) {
            return new LocalMediaVideoInfo(in);
        }

        @Override
        public LocalMediaVideoInfo[] newArray(int size) {
            return new LocalMediaVideoInfo[size];
        }
    };
}
