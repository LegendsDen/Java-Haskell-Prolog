class threading extends Thread {
    int alpha;
    public threading(int no){
        this.alpha=no;
        
    }
    @Override
    public void run(){
        for(int i=1;i<=5;i++){
            System.out.println(i+ "Thread "+alpha);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {  
            }
        }

    }
   
}
public class Main{
    public  static void main(String []args){
        for(int j=0;j<=3;j++){
            threading alpha=new threading (j);
            Thread myThread =new Thread(alpha);
            myThread.start(); // initiates the thread and calls run()
            // alpha.start();
        }




    }
}

// public class threading implements Runnable {
//     int alpha;
//     public threading(int no){
//         this.alpha=no;
        
//     }
//     @Override
//     public void run(){
//         for(int i=1;i<=5;i++){
//             System.out.println(i+ "Thread "+alpha);
//             try {
//                 Thread.sleep(1000);
//             } catch (InterruptedException e) {  
//             }
//         }

//     }
   
// }
