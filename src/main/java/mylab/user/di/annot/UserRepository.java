package mylab.user.di.annot;

public class UserRepository {
   private String dbType;
    
    public UserRepository() {}
    
    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }
    
    public boolean saveUser(String userId, String name) {
        System.out.println("사용자 저장: " + userId + ", " + name + " (DB: " + dbType + ")");
        return true;
    }
    
    @Override
    public String toString() {
        return "UserRepository [dbType=" + dbType + "]";
    }
}