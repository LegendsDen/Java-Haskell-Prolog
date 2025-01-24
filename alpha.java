import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
public class alpha {
    public static void  main( String[]args){
        // map<int,int>maps;
        String arr[]=new String[5];
        Map<Integer,Integer>maps=new HashMap<>();
        Set<Integer>s=new HashSet<>();

        Vector<Integer> vector = new Vector<>();
        vector.add(1);
        vector.add(5);
        int ck=vector.get(1);
        System.out.println(ck);

        int []vec=new int [5];
        vec[0]=1;
        
        for(int i=0;i<4;i++){
            System.out.print(vec[i]);
            System.out.print(" ");
        }
        char c=args[0].charAt(0);
        System.out.println(c);


    }

    
}
