package demorfid.zebra.atnsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PersonListAdapter extends ArrayAdapter<TagData> {

    private static final String TAG = "PersonListAdapter";
    private Context mContext;
    int mResource;

    public PersonListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<TagData> objects, Context mContext) {
        super(context, resource, objects);
        this.mContext = mContext;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String TagID = getItem(position).getTagID();
        String TagNum = getItem(position).getTagNum();
        String Desc = getItem(position).getDesc();

        TagData tagData = new TagData(TagID, TagNum, Desc);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

//        TextView tvTagID = convertView.findViewById(R.id.textView1);
//        TextView tvTagNum = convertView.findViewById(R.id.textView2);
//        TextView tvDesc = convertView.findViewById(R.id.textView3);

//        tvTagID.setText(TagID);
//        tvTagNum.setText(TagNum);
//        tvDesc.setText(Desc);

        return convertView;
    }
}
