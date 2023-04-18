package person;

import java.util.List;

public class PageResult<T> {

    /**
     * 总条数.
     */
    private long total;
    /**
     * 页码.
     */
    private long page;
    /**
     * 每页条数.
     */
    private long pageSize;
    /**
     * 列表.
     */
    private List<T> items;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
