package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.HistoryAdapter;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel;

import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment {
    private final String TAG = "HistoryFragment";
    private RecyclerView mRecyclerView;
    private ConcatAdapter mAdapter;
    private BookmarkViewModel mViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.
                AndroidViewModelFactory(requireActivity().getApplication())).get(BookmarkViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.generic_loading_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        RecyclerView rv = view.findViewById(R.id.recycler_history);
        view.findViewById(R.id.progress_load).setVisibility(View.GONE);

        mAdapter = new ConcatAdapter();
        mRecyclerView = view.findViewById(R.id.recycler_load);
        mRecyclerView.setAdapter(mAdapter);

        setHistory();
    }

    private void setHistory(){

        new Thread(() -> {
            List<Long> times = mViewModel.getTimes();

            Calendar in = Calendar.getInstance();
            in.set(Calendar.MINUTE, 0);
            in.set(Calendar.HOUR, 0);
            in.set(Calendar.SECOND, 0);
            in.set(Calendar.MILLISECOND, 0);

            long today = in.getTimeInMillis();

            times.forEach(c -> {
                List<DBManga> historyManga = mViewModel.getHistoryManga(c);
                int day = (int) Math.abs(today - c) / (1000*60*60*24);
                String dayTitle = mViewModel.getDayTitle(day);

                requireActivity().runOnUiThread(() -> {
                    HistoryAdapter hAdapter = new HistoryAdapter(requireActivity(), historyManga);
                    TitleAdapter tAdapter = new TitleAdapter(requireActivity(), dayTitle, hAdapter);

                    hAdapter.setOnButtonsClickedListener(onButtonClicked(), onButtonClicked());
                    mAdapter.addAdapter(tAdapter);
                    mAdapter.addAdapter(hAdapter);
                });
            });

        }).start();
    }

    private View.OnClickListener onButtonClicked(){
        return v -> {

            HistoryAdapter.ViewHolder viewHolder = (HistoryAdapter.ViewHolder) mRecyclerView.findContainingViewHolder(v);

            DBManga manga = ((HistoryAdapter) viewHolder.getBindingAdapter()).getMangas().get(viewHolder.getBindingAdapterPosition());

            if(v.getId() == R.id.button_resume) {

                Intent resumeIntent = new Intent(requireActivity(), ReadActivity.class);

                resumeIntent.putExtra(ReadActivity.TAG, manga.getLastChapter().getId());

                ContextCompat.startActivity(requireActivity(), resumeIntent, null);
            }else{
                mViewModel.removeFromHistory(manga.getLastChapter());
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        mAdapter = null;
        mRecyclerView = null;
    }
}
