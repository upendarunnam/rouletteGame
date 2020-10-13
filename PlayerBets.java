public class PlayerBets {

    private String name;
    private String type;
    private String ammount;
    private String status;
    private String price;

    public PlayerBets(String name, String type, String ammount) {
        this.name = name;
        this.type = type;
        this.ammount = ammount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmmount() {
        return ammount;
    }

    public void setAmmount(String ammount) {
        this.ammount = ammount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PlayerBets [ammount=" + ammount + ", name=" + name + ", price=" + price + ", status=" + status
                + ", type=" + type + "]";
    }

}
