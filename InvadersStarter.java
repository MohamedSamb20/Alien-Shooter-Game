import javax.swing.JFrame;

public class InvadersStarter  extends JFrame 
{
	   public InvadersStarter()
	    {
	        add(new GameBoard());
	        setTitle("Space Invaders");
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setSize(500,500);
	        setLocationRelativeTo(null);
	        setVisible(true);
	        setResizable(false);
	    }

	    public static void main(String[] args) {
	        new InvadersStarter();


	    }
}
