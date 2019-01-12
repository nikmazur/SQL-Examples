//Class for holding customer data
public class Customer {
    private String cust_code;
    private String agent_code;
    private String cust_name;

    public Customer(String cust_code, String agent_code, String cust_name) {
        this.cust_code = cust_code;
        this.agent_code = agent_code;
        this.cust_name = cust_name;
    }

    // https://www.mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
    // Overriding the equals method to allow comparison between Customer objects
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Customer)) {
            return false;
        }

        Customer cust = (Customer) o;

        //Returns true if all 3 strings in both objects are equal, false otherwise
        return cust.cust_code.equals(cust_code) &&
                cust.agent_code.equals(agent_code) &&
                cust.cust_name.equals(cust_name);
    }
}
