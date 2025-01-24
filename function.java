public class function{
    public  static void main(String []args){
        for(int j=0;j<=3;j++){
            threading alpha=new threading (j);
            Thread myThread =new Thread(alpha);
            myThread.start();
            // alpha.start();
        }




    }
}
