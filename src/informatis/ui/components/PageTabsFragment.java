package informatis.ui.components;

import arc.func.Prov;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import informatis.SUtils;

public class PageTabsFragment extends TabsFragment {
    public final Table content;

    public PageTabsFragment(Object... tabPages) {
        this(new Table(), tabPages);
    }

    public PageTabsFragment(Table content, Object... tabPages) {
        super(SUtils.pickFromArray(tabPages, String.class, i -> i % 2 == 0));
        Object[] pages = SUtils.pickFromArray(tabPages, Object.class, i -> i % 2 != 0);

        this.content = content;
        this.content.add(buildPage(pages[0])).grow();
        eventEmitter.subscribe(TabsFragment.Event.TabChanged, () -> {
            this.content.clearChildren();
            this.content.add(buildPage(pages[currentTabIndex])).grow();
        });
    }

    private Element buildPage(Object page) {
        if(page instanceof Element element) {
            return element;
        } else {
            Prov<Element> prov = (Prov<Element>) page;
           return prov.get();
        }
    }
}
