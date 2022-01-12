package com.labters.documentscannerandroid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.labters.documentscannerandroid.api.Uploader;
import com.labters.documentscannerandroid.api.model.FilesListResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;

public class ServerFilesListActivity extends AppCompatActivity {

    private static final String EXTRA_DATE = "ServerFilesListActivity.EXTRA_DATE";

    CompositeDisposable disposable = new CompositeDisposable();

    RecyclerView recyclerView;
    Odaoter adapter;

    public static void start(Context caller, String date){
        Intent intent = new Intent(caller, ServerFilesListActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        caller.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_files_view);

        recyclerView = findViewById(R.id.list);
        adapter = new Odaoter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        Uploader uploader = new Uploader();

        disposable.add(uploader.getUploadAPI().getFilesList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<FilesListResponse, Throwable>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void accept(FilesListResponse filesListResponse, Throwable throwable) throws Exception {
                        if (filesListResponse == null){
                            Toast.makeText(getApplicationContext(), "get error", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            adapter.init(filesListResponse.getFiles());
                        }

                    }
                }));
    }


    public static class Odaoter extends RecyclerView.Adapter<PhotoItemViewHolder> {

        private ArrayList<String> filesList = new ArrayList<>();

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void init(List<String> files){
            this.filesList.clear();

            List<String> sortedList = files.stream()
                    .sorted(Comparator.comparing((String string) -> string.charAt(string.length() - 1))
                            .thenComparing(Comparator.naturalOrder()))
                    .collect(Collectors.toList());
            this.filesList.addAll(sortedList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PhotoItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            return new PhotoItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_photo, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoItemViewHolder holder, int position) {
            holder.bind(filesList.get(position));
        }

        @Override
        public int getItemCount() {
            return filesList.size();
        }
    }

    private static class PhotoItemViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        String filename;

        public PhotoItemViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PhotoActivity.start(view.getContext(), filename);
                }
            });
        }

        public void bind(String filename) {
            text.setText(filename);
            this.filename = filename;
        }
    }
}