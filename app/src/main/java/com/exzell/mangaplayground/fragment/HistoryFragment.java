package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.HistoryAdapter;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {
    private BookmarkViewModel mViewModel;
    private GenericLoadingRecyclerViewBinding mBinding;


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
        mBinding = GenericLoadingRecyclerViewBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mBinding.progressLoad.setVisibility(View.GONE);

        ConcatAdapter mAdapter = new ConcatAdapter();
        mBinding.recyclerLoad.setAdapter(mAdapter);

        setHistory();
    }

    private void setHistory(){

        new Thread(() -> {
            List<Long> times = mViewModel.getTimes();
            List<Long> foundMangas = new ArrayList<>();

            Calendar in = Calendar.getInstance();
            in.set(Calendar.MINUTE, 0);
            in.set(Calendar.HOUR, 0);
            in.set(Calendar.SECOND, 0);
            in.set(Calendar.MILLISECOND, 0);

            long today = in.getTimeInMillis();

            times.forEach(c -> {
                List<DBManga> historyManga = mViewModel.getHistoryManga(c).stream()
                        .filter(man -> !foundMangas.contains(man.getId())).collect(Collectors.toList());
                foundMangas.addAll(historyManga.stream().map(man -> man.getId()).collect(Collectors.toList()));

                if(historyManga.isEmpty()) return;

                int day = (int) Math.abs(today - c) / (1000*60*60*24);
                String dayTitle = mViewModel.getDayTitle(day);

                requireActivity().runOnUiThread(() -> {
                    HistoryAdapter hAdapter = new HistoryAdapter(requireActivity(), historyManga);
                    TitleAdapter tAdapter = new TitleAdapter(requireActivity(), dayTitle, hAdapter);

                    hAdapter.setOnButtonsClickedListener(onButtonClicked(), onButtonClicked());

                    ((ConcatAdapter) mBinding.recyclerLoad.getAdapter()).addAdapter(tAdapter);
                    ((ConcatAdapter) mBinding.recyclerLoad.getAdapter()).addAdapter(hAdapter);
                });
            });

        }).start();
    }

    private View.OnClickListener onButtonClicked(){
        return v -> {

            HistoryAdapter.ViewHolder viewHolder = (HistoryAdapter.ViewHolder) mBinding.recyclerLoad.findContainingViewHolder(v);

            DBManga manga = ((HistoryAdapter) viewHolder.getBindingAdapter()).getMangas().get(viewHolder.getBindingAdapterPosition());

            if(v.getId() == R.id.button_resume) {

                Intent resumeIntent = new Intent(requireActivity(), ReadActivity.class);

                resumeIntent.putExtra(ReadActivity.CHAPTER, manga.getLastChapter().getId());

                ContextCompat.startActivity(requireActivity(), resumeIntent, null);
            }else{
                mViewModel.removeFromHistory(manga.getLastChapter());
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.recyclerLoad.setAdapter(null);
        mBinding = null;
    }
}
