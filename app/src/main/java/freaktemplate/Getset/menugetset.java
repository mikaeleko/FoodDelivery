package freaktemplate.Getset;

/**
 * Created by Redixbit 2 on 01-09-2016.
 */
public class menugetset {
    private String status;
    private String Menu_Category;
    private String id;
    private String name;
    private String created_at;

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMenu_Category() {
        return Menu_Category;
    }

    public void setMenu_Category(String menu_Category) {
        Menu_Category = menu_Category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
