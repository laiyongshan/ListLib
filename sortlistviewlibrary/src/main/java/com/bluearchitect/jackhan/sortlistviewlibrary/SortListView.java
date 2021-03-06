package com.bluearchitect.jackhan.sortlistviewlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hhz
 * @time 2016/11/12 16:44
 * @description 字母检索List控件
 */

public class SortListView extends LinearLayout implements SearchView.OnCloseListener, SideBar.OnTouchingLetterChangedListener, SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    private int letters_bg;
    private int letters_text_color;
    private int letters_text_size;
    private int sidebar_bg;
    private int sidebar_text_color;
    private int sidebar_text_color_pressed;
    private int sidebar_cell_spacing;
    private int dialog_bg;
    private int dialog_text_color;
    private int dialog_text_size;
    private int firstwords_bg;
    private int firstwords_text_color;
    private int firstwords_text_size;

    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    SearchView searchView;

    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;

    List<SortModel> sortList;

    public SortListView(Context context) {
        this(context, null);
    }

    public SortListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SortListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SortListView,
                defStyleAttr, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.SortListView_letters_bg) {
                letters_bg = typedArray.getColor(attr, getResources().getColor(R.color.letters_bg));
            } else if (attr == R.styleable.SortListView_letters_text_color) {
                letters_text_color = typedArray.getColor(attr, getResources().getColor(R.color.letters_text));
            } else if (attr == R.styleable.SortListView_letters_text_size) {
                // 默认设置为16sp，TypeValue也可以把sp转化为px
                letters_text_size = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.SortListView_sidebar_bg) {
                sidebar_bg = typedArray.getResourceId(attr, R.drawable.sidebar_background);
            } else if (attr == R.styleable.SortListView_sidebar_text_color) {
                sidebar_text_color = typedArray.getColor(attr,
                        getResources().getColor(R.color.sidebar_text));
            } else if (attr == R.styleable.SortListView_sidebar_text_color_pressed) {
                sidebar_text_color_pressed = typedArray.getColor(attr,
                        getResources().getColor(R.color.sidebar_text_pressed));
            } else if (attr == R.styleable.SortListView_sidebar_cell_spacing) {
                sidebar_cell_spacing = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.SortListView_dialog_bg) {
                dialog_bg = typedArray.getResourceId(attr, R.drawable.show_head_toast_bg);
            } else if (attr == R.styleable.SortListView_dialog_text_color) {
                dialog_text_color = typedArray.getColor(attr,
                        getResources().getColor(R.color.sort_dialog_text));
            } else if (attr == R.styleable.SortListView_dialog_text_size) {
                dialog_text_size = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.SortListView_firstwords_dialog_bg) {
                firstwords_bg = typedArray.getResourceId(attr, R.drawable.first_word_bg);
            } else if (attr == R.styleable.SortListView_firstwords_dialog_text_color) {
                firstwords_text_color = typedArray.getColor(attr,
                        getResources().getColor(R.color.firstwords_dialog_text));
            } else if (attr == R.styleable.SortListView_firstwords_dialog_text_size) {
                firstwords_text_size = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()));
            }

        }
        typedArray.recycle();

        LayoutInflater.from(context).inflate(R.layout.sort_layout, this, true);

        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        if (dialog_bg != 0)
            dialog.setBackgroundResource(dialog_bg);
        if (dialog_text_size != 0)
            dialog.setTextSize(dialog_text_size);
        if (dialog_text_color != 0)
            dialog.setTextColor(dialog_text_color);
        sideBar.setTextView(dialog);

        sideBar.setViewAttr(sidebar_cell_spacing, sidebar_text_color,
                sidebar_text_color_pressed, sidebar_bg);

        sideBar.setOnTouchingLetterChangedListener(this);

        sortListView = (ListView) findViewById(R.id.sort_contentListView);
        sortListView.setOnItemClickListener(this);

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setIconified(false);
    }
/*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SortListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    /**
     * 给列表设置adapter
     *
     * @param adapter 适配器
     */
    public void setAdapter(SortAdapter adapter) {
        adapter.setLettersAttr(letters_bg, letters_text_color, letters_text_size);
        this.sortList = adapter.sortList;
        this.adapter = adapter;
        sortListView.setAdapter(adapter);
    }


    /**
     * 根据sortName查找数据
     *
     * @param filterSortName
     */
    private void filterData(String filterSortName) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterSortName)) {
            filterDateList = sortList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : sortList) {
                String name = sortModel.getSortName();
                if (name.indexOf(filterSortName.toString()) != -1
                        || characterParser.getSelling(name).startsWith(filterSortName.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }


    /**
     * 根据首字字母获取所有首字第一次出现的位置
     *
     * @param letter 首字母
     * @return 所有首字第一次出现位置的map
     */
    public Map<String, Integer> filterFristWordPositionsByLetter(String letter) {
        Map<String, Integer> fristWordPositionMap = new HashMap<>();

        if (TextUtils.isEmpty(letter)) {
            return fristWordPositionMap;
        } else {
            for (int i = 0; i < sortList.size(); i++) {
                SortModel sortModel = sortList.get(i);
                if (letter.equals(sortModel.getSortLetters())) {
                    String fristWord = sortModel.getSortName().substring(0, 1);
                    if (!fristWordPositionMap.containsKey(fristWord)) {
                        fristWordPositionMap.put(fristWord, i);
                    }
                }
            }
        }

        return fristWordPositionMap;
    }

    FirstWordsDialog firstWordsDialog;


    /**
     * 显示根据首字字母获取所有首字
     *
     * @param fristWordsPositionMap 所有首字
     * @param position              字母位置
     * @param w                     sidebar宽度
     * @param itemH                 sidebar cell 高度
     */
    @SuppressLint("NewApi")
    public void showFistWordsDialog(final Map<String, Integer> fristWordsPositionMap, final int position,
                                    int w, int itemH) {

        if (firstWordsDialog != null && firstWordsDialog.isShowing())
            firstWordsDialog.dismiss();
        if (fristWordsPositionMap.size() > 0) {
            if (firstWordsDialog == null) {
                firstWordsDialog = new FirstWordsDialog(getContext(), firstwords_bg, firstwords_text_color, firstwords_text_size, new FirstWordsDialog.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        sortListView.setSelection(position);
                    }
                });
            }
            firstWordsDialog.setFirstWords(fristWordsPositionMap);
            int firstWordsCellHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());
            int firstWordsDialogHeight = firstWordsCellHeight * (fristWordsPositionMap.size() / 3)
                    + firstWordsCellHeight * (fristWordsPositionMap.size() % 3 > 0 ? 1 : 0);
            firstWordsDialog.showAsDropDown(searchView, -w, itemH * position + itemH / 2 - firstWordsDialogHeight / 2, Gravity.RIGHT);

            //Toast.makeText(getContext(), fristWordsPositionMap.keySet().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onClose() {

        adapter.updateListView(sortList);
        return false;
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = adapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            sortListView.setSelection(position);
        }
    }

    public boolean isShowFirstWordsByLetter() {
        return isShowFirstWordsByLetter;
    }

    /**
     * 是否显示字母检索后的所有首字
     *
     * @param showFirstWordsByLetter 是否
     */
    public void setShowFirstWordsByLetter(boolean showFirstWordsByLetter) {
        isShowFirstWordsByLetter = showFirstWordsByLetter;
    }

    boolean isShowFirstWordsByLetter = false;

    @Override
    public void onTouchedUpLetter(String s, int position, int w, int itemH) {
        if (isShowFirstWordsByLetter)
            showFistWordsDialog(filterFristWordPositionsByLetter(s), position, w, itemH);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        filterData(newText);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if (onItemClickListener != null)
            onItemClickListener.onItemClick((SortModel) adapter.sortList.get(i), adapterView, view, i, l);
    }

    OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener<Item extends SortModel> {
        void onItemClick(Item item, AdapterView<?> var1, View var2, int var3, long var4);
    }
}
