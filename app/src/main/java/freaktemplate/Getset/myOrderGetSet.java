package freaktemplate.Getset;

/**
 * Created by RedixbitUser on 3/23/2018.
 */

public class myOrderGetSet {
    private String resName;
    private String resAddress;
    private String order_dateTime;
    private String order_id;
    private String order_total;

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResAddress() {
        return resAddress;
    }

    public void setResAddress(String resAddress) {
        this.resAddress = resAddress;
    }

    public String getOrder_dateTime() {
        return order_dateTime;
    }

    public void setOrder_dateTime(String order_dateTime) {
        this.order_dateTime = order_dateTime;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_total() {
        return order_total;
    }

    public void setOrder_total(String order_total) {
        this.order_total = order_total;
    }
}
