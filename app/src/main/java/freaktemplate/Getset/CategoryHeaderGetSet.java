package freaktemplate.Getset;

/**
 * Created by Redixbit 2 on 08-09-2016.
 */
public class CategoryHeaderGetSet {
    private String id, name;
    private String[] subcategoryid;
    private String[] setSubcategoryname;


    public String[] getSubcategoryid() {
        return subcategoryid;
    }

    public String[] getSubcategoryname() {
        return setSubcategoryname;
    }

    public void setSubcategoryname(String[] setSubcategoryname) {
        this.setSubcategoryname = setSubcategoryname;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setSubcategoryid(String[] subcategoryid) {
        this.subcategoryid = subcategoryid;
    }
}
