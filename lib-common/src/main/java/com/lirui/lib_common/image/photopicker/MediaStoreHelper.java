package com.lirui.lib_common.image.photopicker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lirui.lib_common.R;
import com.lirui.lib_common.image.photopicker.bean.PhotoDirectory;
import com.lirui.lib_common.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;

/**
 * 获取图片相册
 */
public class MediaStoreHelper {

    public final static int INDEX_ALL_PHOTOS = 0;
    public static final String PARENT_PATH = "all";

    public static void getPhotoDirs(FragmentActivity activity, Bundle args, PhotosResultCallback resultCallback) {
        activity.getSupportLoaderManager()
                .initLoader(0, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    }

    /**
     * 处理与LoaderManager交互
     */
    private static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private Context context;
        private PhotosResultCallback resultCallback;

        public PhotoDirLoaderCallbacks(Context context, PhotosResultCallback resultCallback) {
            this.context = context;
            this.resultCallback = resultCallback;
        }

        /**
         * 根据指定的 ID 初始化并创建一个新的 loader
         */
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new PhotoDirectoryLoader(context, args.getBoolean(PhotoPickerActivity.EXTRA_SHOW_GIF, false));
        }

        /**
         * 当之前 loader 完成了数据加载后调用
         */
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (data == null) return;
            List<PhotoDirectory> directories = new ArrayList<>();
            PhotoDirectory photoDirectoryAll = new PhotoDirectory();
            photoDirectoryAll.setName(context.getString(R.string.picker_all_image));
            photoDirectoryAll.setId("ALL");
            photoDirectoryAll.setSelected(true);

            while (data.moveToNext()) {
                int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
                String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = data.getString(data.getColumnIndexOrThrow(DATA));
                long size = data.getInt(data.getColumnIndexOrThrow(SIZE));

                if (size < 1) continue;

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(FileUtils.getUrlPath(path));
                    photoDirectory.setImagePath(path);
                    photoDirectory.addPhoto(imageId, path);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, path);
                }

                photoDirectoryAll.addPhoto(imageId, path);
            }
            if (photoDirectoryAll.getPhotoPaths().size() > 0) {
                photoDirectoryAll.setImagePath(photoDirectoryAll.getPhotoPaths().get(0));
                photoDirectoryAll.setCoverPath(PARENT_PATH);
            }
            directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);
            if (resultCallback != null) {
                resultCallback.onResultCallback(directories);
            }
        }

        /**
         * 当之前的 loader 被重置时调用，这样，它不能在提供数据
         */
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    /**
     * 图片获取完成
     */
    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }

}
