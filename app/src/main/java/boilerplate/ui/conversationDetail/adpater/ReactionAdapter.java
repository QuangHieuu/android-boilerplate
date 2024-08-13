package boilerplate.ui.conversationDetail.adpater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import boilerplate.R;
import boilerplate.model.message.Reaction;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ReactionVH> {
    private final ArrayList<Reaction> mList = new ArrayList<>();
    private final OnItemListener mListener;

    public ReactionAdapter(OnItemListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ReactionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_reaction, parent, false);
        return new ReactionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionVH holder, int position) {
        holder.setData(mList.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addData(ArrayList<Reaction> items) {
        int size = mList.size();
        if (size > 0) {
            mList.clear();
            notifyItemRangeRemoved(0, size);
        }
        mList.addAll(items);
        notifyItemRangeInserted(0, items.size());
    }

    public void reactionIcon(Reaction item) {
        int index = mList.indexOf(item);
        item.setReacted(!item.isReacted());
        notifyItemChanged(index, item);
    }

    public interface OnItemListener {
        void onChosenReaction(Reaction reaction);
    }

    protected static class ReactionVH extends RecyclerView.ViewHolder {
        private final TextView tvIcon;
        private final View dot;

        public ReactionVH(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            dot = itemView.findViewById(R.id.view_dot);
        }

        public void setData(Reaction reaction, OnItemListener mListener) {
            tvIcon.setText(reaction.getEmoticonName());
            tvIcon.setOnClickListener(view -> mListener.onChosenReaction(reaction));
            if (reaction.isReacted()) {
                dot.setVisibility(View.VISIBLE);
            } else {
                dot.setVisibility(View.GONE);
            }
        }
    }
}
