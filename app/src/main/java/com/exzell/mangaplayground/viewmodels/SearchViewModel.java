package com.exzell.mangaplayground.viewmodels;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;

import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.utils.MangaUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends AndroidViewModel {
    private final String TAG = "SearchViewModel";
    private Context mContext;
    private Repository mRepo;
    private SavedStateHandle mHandle;
    private Handler mHandler;

    //KEYS
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "auth/art";
    private final String KEY_TITLE_CONTAIN = "title_extra";
    private final String KEY_AUTHOR_CONTAIN = "auth/art_extra";
    private final String KEY_GENRE = "genres";
    private final String KEY_RATING = "getRating";
    private final String KEY_STATUS = "status";
    private final String KEY_TYPE = "type";
    private final String KEY_CHAPTERS = "chapters";
    private final String KEY_RELEASE = "releases";
    private final String KEY_GENRE_INCL = "genre inclusion";
    private final String KEY_ORDER = "order";

    private final String KEY_LINK = "last search link";


    public static final List<String> statusData = Stream.of(MangaSearch.STATUS_COMPLETED, MangaSearch.STATUS_ONGOING).map(String::toUpperCase).collect(Collectors.toList());
    public static final List<Integer> chapterData = Stream.of(1, 5, 10, 20, 30, 40, 50, 100, 200).collect(Collectors.toList());
    public static final List<String> releaseData = IntStream.rangeClosed(1946, 2017).boxed().map(String::valueOf).collect(Collectors.toList());

    private List<Manga> mCurrentSearchResults = new ArrayList<>();

    public SearchViewModel(@NonNull Application application, SavedStateHandle handle) {
        super(application);
        mContext = application.getApplicationContext();
        mRepo = Repository.getInstance(application);
        mHandle = handle;
        mHandler = new Handler();
    }

    /**
     * Collects a map of search parameters to their values which can be created through {@link #search}
     * and makes the request as well as creating the mangas
     * The consumer is called on the same thread the request was made
     * @param onMangaRetrieved A consumer called when the mangas have been created
     */
    public void resolveSearch(Map<String, String> search, Consumer<List<Manga>> onMangaRetrieved){

            mRepo.advancedSearch(search).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()) {
                        Element html;
                        try {
                            html = Jsoup.parse(response.body().string()).body();
                            response.body().close();

                            String next = html.getElementsContainingText("next▶").attr("href");

                            if (!next.isEmpty()) {

                                Map<String, String> nextLink = new HashMap<>(search);
                                nextLink.put("page", getDigits(next));
                                setNextSearchLink(nextLink);
                            }else setNextSearchLink(null);

                            List<Manga> searchManga = MangaUtils.createSearchManga(html);
                            mCurrentSearchResults.addAll(searchManga);
                            onMangaRetrieved.accept(searchManga);
                            Log.w(TAG, next);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(mContext, "Error Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(mContext,"Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    public List<Manga> getCurrentSearchResults(){return mCurrentSearchResults;}

    public void clearSearchResults(){mCurrentSearchResults.clear();}

    public void setNextSearchLink(Map<String, String> nextLink){
        mHandle.set(KEY_LINK, nextLink);
    }

    public Map<String, String> getNextLink(){return mHandle.get(KEY_LINK);}

    private String getDigits(String string){
        StringBuilder num = new StringBuilder();
        for(char c : string.toCharArray()){
            if(Character.isDigit(c)) num.append(c);
        }

        return num.toString();
    }

    public List<String> getSelectedGenres(){
        return mHandle.get(KEY_GENRE);
    }

    public String getName(boolean which){
        return which ? mHandle.get(KEY_AUTHOR) : mHandle.get(KEY_TITLE);
    }

    public String containValue(boolean which){
        return which ? mHandle.get(KEY_AUTHOR_CONTAIN) : mHandle.get(KEY_TITLE_CONTAIN);
    }

    public int getRating(){return mHandle.get(KEY_RATING);}

    public String getStatus(){
        return mHandle.get(KEY_STATUS);
    }

    public String getType(){
        return mHandle.get(KEY_TYPE);
    }

    public int getChapters(){
        return mHandle.get(KEY_CHAPTERS);
    }

    public int getRelease(){return mHandle.get(KEY_RELEASE);}

    public String getGenreInclusion(){return mHandle.get(KEY_GENRE_INCL);}

    public String getOrder(){return mHandle.get(KEY_ORDER);}

    public void setGenres(String genre, boolean which){
        ArrayList<String> arr = mHandle.get(KEY_GENRE);
        if(which) arr.add(genre);
        else{
            arr.remove(arr.indexOf(genre));
        }
    }

    public void setName(boolean which, String value){
        String key =  which ? KEY_AUTHOR : KEY_TITLE;
        mHandle.set(key, value);
    }

    public void setContainValue(boolean which, String value){
        String key =  which ? KEY_AUTHOR_CONTAIN : KEY_TITLE_CONTAIN;
        mHandle.set(key, value);
    }

    public void setRating(int rating){ mHandle.set(KEY_RATING, rating);}

    public void setStatus(String status){ mHandle.set(KEY_STATUS, status); }

    public void setType(String type){
        mHandle.set(KEY_TYPE, type);
    }

    public void setChapters(int chap){
        mHandle.set(KEY_CHAPTERS, chap);
    }

    public void setRelease(int release){mHandle.set(KEY_RELEASE, release);}

    public void setGenreInclusion(String incl){mHandle.set(KEY_GENRE_INCL, incl);}

    public void setOrder(String order){
        mHandle.set(KEY_ORDER, order);
    }

    public void resetValues(){
        setName(false, "");
        setName(true, "");
        setContainValue(false, "");
        setContainValue(true, "");
        setChapters(-1);
        setRating(-1);
        setRelease(-1);
        setStatus("");
        setType("");
        setGenreInclusion("");
        getSelectedGenres().clear();
    }

    public void handlerDefaults(){
        if(!mHandle.contains(KEY_GENRE)) mHandle.set(KEY_GENRE, new ArrayList<String>());
        if(!mHandle.contains(KEY_TITLE)) mHandle.set(KEY_TITLE, "");
        if(!mHandle.contains(KEY_AUTHOR)) mHandle.set(KEY_AUTHOR, "");
        if(!mHandle.contains(KEY_TITLE_CONTAIN)) mHandle.set(KEY_TITLE_CONTAIN, "");
        if(!mHandle.contains(KEY_AUTHOR_CONTAIN)) mHandle.set(KEY_AUTHOR_CONTAIN, "");
        if(!mHandle.contains(KEY_RATING)) mHandle.set(KEY_RATING, -1);
        if(!mHandle.contains(KEY_STATUS)) mHandle.set(KEY_STATUS, "");
        if(!mHandle.contains(KEY_TYPE)) mHandle.set(KEY_TYPE, "");
        if(!mHandle.contains(KEY_CHAPTERS)) mHandle.set(KEY_CHAPTERS, -1);
        if(!mHandle.contains(KEY_RELEASE)) mHandle.set(KEY_RELEASE, -1);
        if(!mHandle.contains(KEY_GENRE_INCL)) mHandle.set(KEY_GENRE_INCL, "");
        if(!mHandle.contains(KEY_ORDER)) mHandle.set(KEY_ORDER, "");
    }

    public Map<String, String> search() {
        MangaSearch search = new MangaSearch.Builder().setAuthor(getName(true))
                .setAuthorContain(containValue(true))
                .setTitle(getName(false))
                .setTitleContain(containValue(false))
                .setChapters(getChapters())
                .setRating(getRating())
                .setRelease(getRelease())
                .setStatus(getStatus())
                .setType(getType())
                .addGenres(getSelectedGenres())
                .setGenreInclusion(getGenreInclusion())
                .setOrder(getOrder())
                .build();

//        if(search.searchQuery().isEmpty()) return;
        return search.searchQuery();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCurrentSearchResults.clear();
    }
}
