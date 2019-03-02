package dev.pushkar.goconso

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import worldline.com.foldablelayout.FoldableLayout

/**
 * TODO: Add a class header comment!
 */
class PhotoAdapter(private val mDataSet: MutableList<Update>, private var mContext: Context) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val mFoldStates = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(FoldableLayout(parent.context))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (mDataSet[position].sel) {
            val path = mDataSet[position].img
            val picasso = Picasso.Builder(mContext).build()
            picasso.isLoggingEnabled = true
            picasso.load(path).into(holder.mImageViewCover)
            picasso.load(path).into(holder.mImageViewDetail)
        } else {
            val details = mDataSet[position].dt
            val hl = mDataSet[position].hl
            holder.mTextViewDetail.text = details
            holder.mTextViewDetail.visibility = View.VISIBLE
            holder.mTextHeader.visibility = View.VISIBLE
            holder.mTextHeader.text = hl
        }
        holder.mTextViewCover.text = mDataSet[position].hl

        if (mDataSet[position].btn) {
            holder.mButtonShare.visibility = View.VISIBLE
            holder.mButtonShare.text = mDataSet[position].btnTxt
        } else {
            holder.mButtonShare.visibility = View.GONE
            holder.mButtonShare.text = mDataSet[position].btnTxt
        }

        // Bind state
        if (mFoldStates.containsKey(position)) {
            if (mFoldStates[position] === java.lang.Boolean.TRUE) {
                if (!holder.mFoldableLayout.isFolded) {
                    holder.mFoldableLayout.foldWithoutAnimation()
                }
            } else if (mFoldStates[position] === java.lang.Boolean.FALSE) {
                if (holder.mFoldableLayout.isFolded) {
                    holder.mFoldableLayout.unfoldWithoutAnimation()
                }
            }
        } else {
            holder.mFoldableLayout.foldWithoutAnimation()
        }

        if (mDataSet[position].btn) {

            holder.mButtonShare.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mDataSet[position].url))
                mContext.startActivity(intent)
            }
        } else {
            holder.mButtonShare.visibility = View.GONE
        }
        holder.mFoldableLayout.setOnClickListener {
            if (holder.mFoldableLayout.isFolded) {
                holder.mFoldableLayout.unfoldWithAnimation()
            } else {
                holder.mFoldableLayout.foldWithAnimation()
            }
        }
        holder.mFoldableLayout.setFoldListener(object : FoldableLayout.FoldListener {
            override fun onUnFoldStart() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.elevation = 5f
                }
            }

            override fun onUnFoldEnd() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.elevation = 0f
                }
                mFoldStates[holder.adapterPosition] = false
            }

            override fun onFoldStart() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.elevation = 5f
                }
            }

            override fun onFoldEnd() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.elevation = 0f
                }
                mFoldStates[holder.adapterPosition] = true
            }
        })
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    class PhotoViewHolder(var mFoldableLayout: FoldableLayout) : RecyclerView.ViewHolder(mFoldableLayout) {
        val mImageViewCover: ImageView
        val mTextHeader: TextView
        val mImageViewDetail: ImageView
        val mTextViewDetail: TextView
        val mTextViewCover: TextView
        val mButtonShare: Button

        init {
            mFoldableLayout.setupViews(R.layout.list_item_cover, R.layout.list_item_detail, R.dimen.card_cover_height, itemView.context)
            mImageViewCover = mFoldableLayout.findViewById(R.id.imageview_cover)
            mTextHeader = mFoldableLayout.findViewById(R.id.pushkar_header)
            mImageViewDetail = mFoldableLayout.findViewById(R.id.imageview_detail)
            mTextViewDetail = mFoldableLayout.findViewById(R.id.pushkar_detail)
            mTextViewCover = mFoldableLayout.findViewById(R.id.textview_cover)
            mButtonShare = mFoldableLayout.findViewById(R.id.share_button)
        }
    }
}