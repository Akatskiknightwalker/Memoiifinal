package com.akatski.memoii.utils;

import java.util.List;

public interface IFirebaseLoadDone {
     void onFirebaseLoadSuccess( List<meme> memeList);

      void onFirebaeLoadFailed(String message);

}
