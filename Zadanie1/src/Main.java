public class Main 
{
    public static void main(String[] args) 
    {
       Bob bob = new Bob();
       Alice alice = new Alice(bob);
       bob.start();
       alice.start();
    }
}

class Bob extends Thread
{
    private int Input;
    
    void SetInput(int input)
    {
        Input = input;
    }
    
    @Override
    public void run()
    {
        while(true) 
        {
            if(interrupted()) 
            {
                if(Input == 0)
                {
                    System.out.println("[Bob] Finishing work");
                    break;
                }
                else
                {
                    SetInput(Input*2);
                    System.out.println("[Bob] The result is: " + Input);
                }
            }
        }    
    }
}

class Alice extends Thread
{
    Bob BobThread;
    
    public Alice(Bob bob)
    {
        this.BobThread = bob;
    }
    
    @Override
    public void run()
    {
       for(int i = 1; i <= 10; i++) 
       {
          BobThread.SetInput(i);
          System.out.println("[Alice] Sending to Bob: " + i);
          BobThread.interrupt();
          
          try 
          {
              sleep(1000);
          } 
          catch (InterruptedException e) 
          {
              throw new RuntimeException(e);
          }
       }
       BobThread.SetInput(0);
       BobThread.interrupt();
    }
}