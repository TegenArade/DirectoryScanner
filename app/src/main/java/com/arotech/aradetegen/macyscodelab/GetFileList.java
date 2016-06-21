package com.arotech.aradetegen.macyscodelab;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by arade.tegen on 6/14/16.
 */
public abstract class GetFileList {
    private ArrayList<FileInfo> fileInfos = new ArrayList<>();
    private Map<String, Integer> fileTypes = new HashMap<>();

    public Observable<FileInfo> getObservable() {

        return Observable.create(new Observable.OnSubscribe<FileInfo>() {
            @Override
            public void call(Subscriber<? super FileInfo> subscriber) {
                try {
                    File EXTERNAL = Environment.getExternalStorageDirectory();
                    scan(EXTERNAL, subscriber);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * This method will do implement a recursive algorithm to do the scanning of files in directories and sub directories.
     * If aborted it will stop scanning.
     */
    static String scan(File directory, Subscriber<? super FileInfo> subscriber) {

        ArrayList<File> listFile = new ArrayList<>(Arrays.asList(directory.listFiles()));
        for (File aListFile : listFile) {
            if (aListFile.isDirectory()) {
                if (!subscriber.isUnsubscribed()) {
                    scan(aListFile, subscriber);
                } else {
                    subscriber.onCompleted();
                    //Log.e("TAG", "aborted!!!!!!!!!!!");
                }
            } else {
                FileInfo fileInfo = new FileInfo(aListFile);
                //Log.d("TAG", fileInfo.toString());
                subscriber.onNext(fileInfo);
            }
        }
        return null;
    }

    public Subscriber<FileInfo> getSubscriber() {

        return new Subscriber<FileInfo>() {
            @Override
            public void onNext(FileInfo fileInfo) {
                fileInfos.add(fileInfo);
                fileTypes.put(fileInfo.getFileType(),
                        (fileTypes.containsKey(fileInfo.getFileType()) ? fileTypes.get(fileInfo.getFileType()) + 1 : 1));
            }

            @Override
            public void onCompleted() {
                if (!isUnsubscribed()) {
                    onSuccessfullyCompleted(new DirectorySummery(fileInfos, fileTypes));
                }
                unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                onCompletedWithError(e.getLocalizedMessage());
            }
        };
    }

    public abstract void onSuccessfullyCompleted(DirectorySummery directorySummery);

    public abstract void onCompletedWithError(String error);
}
