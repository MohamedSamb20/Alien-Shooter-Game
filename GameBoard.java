
/**
 * A player tries to fight away thirty aliens by shooting lasers at them.
 *
 * @author Mohamed Samb
 * @version 4/14/19
 */
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;

public class GameBoard extends JPanel implements Runnable, MouseListener
{
    private Dimension d;
    int BOARD_WIDTH=500;
    int BOARD_HEIGHT=500;
    int x = 0;
    BufferedImage img;
    private Thread animator;
    
    Alien[] aliens = new Alien[30];
    boolean[] showAlien = new boolean[30];
    Barrier[] barriers = new Barrier[4];
    boolean[] showBarrier = new boolean[4];
    int[] Light = new int[4];
    Player you;
    Laser playlaser;
    Laser alienlaser;
    
    boolean ingame = false;
    int ASideMov = -1;
    int ADownMov = 10;
    boolean playerLeft = false;
    boolean playerRight = false;
    int waitcount = 0;
    int score = 0;
    int lives = 3;
    boolean winner = false;
    boolean play = false;
    String message = "Click board to start game";
    
    public GameBoard()
    {
        addKeyListener(new TAdapter());
        addMouseListener(this);
        setFocusable(true);
        d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
        setBackground(Color.black);
        if (animator == null)
        {
            animator = new Thread(this);
            animator.start();
        }
        int rowpos = 40;
        int colpos = 40;
        int count = 1;
        int alienSize = 20;
        for(int i=0; i<aliens.length; i++)
        {
            aliens[i] = new Alien(rowpos, colpos, alienSize, alienSize);//creates 30 aliens
            showAlien[i] = true;//makes the aliens appear on the screen
            rowpos += 40;//shifts next alien right 40
            if(count%10 == 0)//if there is a row of ten
            {
                rowpos = 40;//returns next alien to the first column
                colpos += 40;//creates a new row of aliens
                alienSize += 5;//increases the size of the new aliens
            }
            count++;
        }
        for(int i=0; i<barriers.length; i++)
        {
            barriers[i] = new Barrier(rowpos, 400, 50, 25);//creates 4 barriers
            showBarrier[i] = true;//makes the barriers appear on the screen
            rowpos += 100;//shifts the next barrier right 100
            count++;
        }
        for(int i=0; i<Light.length; i++)
        {
            Light[i]=10;//assigns the starting lightness of the barriers
        }
        you = new Player(250, 450, 30, 15);//creates the player
        waitcount = 0;//creates a counter for certain actions
        alienlaser = new Laser(0,0);//creates a laser object for the alien
        playlaser = new Laser(0,0);//creates a laser object for the player
        setDoubleBuffered(true);
    }
    
    public void paint(Graphics g)
    {
        super.paint(g);
        
        g.setColor(Color.black);
        g.fillRect(0, 0, 500, 500);//sets black background
        
        g.setColor(Color.white);
        Font tr = new Font("TimesRoman", Font.PLAIN, 12);
        g.setFont(tr);
        g.drawString(message,350,10);//sets white message at the top right corner
        
        for(int i=0; i<aliens.length; i++)
        {
            if(showAlien[i])//if the alien hasnt been hit yet...
            {
                g.setColor(Color.blue);
                g.fillOval(aliens[i].x, aliens[i].y,aliens[i].w, aliens[i].h);//makes 30 blue aliens
            }
        }
        
        for(int i=0; i<barriers.length; i++)
        {
            if(showBarrier[i])//if the barrier hasnt been hit 10 times yet...
            {
                int col = 25 * Light[i];
                Color barCol = new Color(col, col, col);//creates a color value based on the number of hits
                g.setColor(barCol);
                g.fillRect(barriers[i].x, barriers[i].y, barriers[i].w, barriers[i].h);//makes 4 blocks
            }
        }
        
        if(alienlaser.isVisible)//if alien laser is activated
        {
            g.setColor(Color.orange);
            g.fillRect(alienlaser.x, alienlaser.y, 5, 10);//creates falling orange laser
        }
        
        if(playlaser.isVisible)//if player laser is activated
        {
            g.setColor(Color.cyan);
            g.fillRect(playlaser.x, playlaser.y, 5, 10);//creates rising blue laser
        }
        
        g.setColor(Color.green);
        g.fillOval(you.x, you.y, you.w, you.h);//creates blue player
        
        Font AGame = new Font("TimesRoman", Font.PLAIN, 18);
        if(winner && play)//if you won the game
        {
            g.setFont(AGame);
            g.drawString("WINNER", 250, 250);//makes message saying winner
        }
        if(winner == false && play)//if you lost the game
        {
            g.setFont(AGame);
            g.drawString("LOSER", 250, 250);//makes message saying you lost
        }
        if(ingame) 
        {
            conditions();
            AlienMovement();
            PlayerMovement();
            LaserMovement();
            message = "Lives: " + lives + " / Score: " + score;//updates score
        }
    }
    public void AlienMovement()
    {
        if(waitcount%5==0)//move every 5 time periods
        {
            for(int i = 0; i<aliens.length; i++)
            {
                if(showAlien[i])
                {
                    if(aliens[i].x == 0 || aliens[i].x == 500)//if alien hits sides...
                    {
                        for(int b = 0; b<aliens.length; b++)
                        {
                            aliens[b].y += ADownMov;//aliens move down 10 pixels
                        }
                        ASideMov *= -1;//move opposite way
                        i = aliens.length;
                    }
                }
            }
            for(int b = 0; b<aliens.length; b++)
            {
                aliens[b].x += ASideMov;//shifts left/right a pixel
            } 
        }
        waitcount++;//a time period has period
    }
    public void PlayerMovement()
    {
        if(playerRight)//if right movement requested...
        {
            you.x += 5;//move right 5 pixels
        }
        if(playerLeft)//if left movement requested
        {
            you.x -= 5;//move left 5 pixels
        }
        
    }
    public void LaserMovement()
    {
        if(playlaser.isFired)//if player laser is requested...
        {
            playlaser.x = you.x;
            playlaser.y = you.y;//laser starts at player position
            playlaser.isVisible = true;//allows laser to be shown and move
            playlaser.isFired = false;//doesnt restart laser positioning
        }
        if(playlaser.isVisible)//is laser has started...
        {
            playlaser.y -= 3;//moves up 3 pixels
        }
        if(playlaser.y <= 0)//when laser hits the top of the screen...
        {
            playlaser.isVisible = false;//laser disappears
        }
        if(waitcount%30 == 0 && alienlaser.isVisible == false)//if alien laser is requested and isnt already on...
        {
            boolean wrong = true;//variable to control possible laser positions
            int alien = 0;
            while(wrong)
            {
                alien = (int)(Math.random()*30);//picks random alien to shoot laser
                if(showAlien[alien])//if alien is still alive...
                {
                    wrong = false;//alien stays
                }
            }
            alienlaser.x = aliens[alien].x;
            alienlaser.y = aliens[alien].y;//laser starts at random alien position
            alienlaser.isVisible = true;//allows laser to be shown, move, and not restart positioning
        }
        if(alienlaser.isVisible)//if laser has been started...
        {
            alienlaser.y += 3;//laser moves down 3 pixels
        }
        if(alienlaser.y >= 500)//if laser hits bottom of screen...
        {
            alienlaser.isVisible = false;//laser disappears
        }
    }
    public void conditions()
    {
        for(int i = 0; i<aliens.length; i++)
        {
            if(playlaser.x>=aliens[i].x && playlaser.x<=aliens[i].x +aliens[i].w && playlaser.y>=aliens[i].y && playlaser.y<=aliens[i].y +aliens[i].h && showAlien[i] && playlaser.isVisible)//if player laser hits an alien...
            {
                showAlien[i] = false;//alien disappears
                if(i<10)//if top row of lasers...
                {
                   score += 200;//adds 200 to player score
                }
                else if(i<20)//if middle row of lasers...
                {
                    score += 100;//adds 100 to player score
                }
                else//if bottom row of lasers...
                {
                    score += 50;//adds 50 to player score
                }
                i = aliens.length;
                playlaser.isVisible = false;//laser disappears
            }
        }
        for(int i = 0; i<barriers.length; i++)
        {
            if(alienlaser.x>=barriers[i].x && alienlaser.x<=barriers[i].x +barriers[i].w && alienlaser.y>=barriers[i].y && alienlaser.y<=barriers[i].y +barriers[i].h && showBarrier[i] && alienlaser.isVisible)//if alien laser hits a barrier...
            {
                Light[i] = Light[i]- 1;//barrier becomes lighter
                if(Light[i] == 0)//if barrier gets hit ten times...
                {
                    showBarrier[i] = false;//barrier disappears
                }
                i = barriers.length;
                alienlaser.isVisible = false;//laser disappears
            }
        }
        for(int i = 0; i<barriers.length; i++)
        {
            if(playlaser.x>=barriers[i].x && playlaser.x<=barriers[i].x +barriers[i].w && playlaser.y>=barriers[i].y && playlaser.y<=barriers[i].y +barriers[i].h && showBarrier[i] && playlaser.isVisible)//if player laser hits a barrier...
            {
                Light[i] = Light[i]- 1;//barrier becomes lighter
                if(Light[i] == 0)//if barrier gets hit ten times...
                {
                    showBarrier[i] = false;//barrier disappears
                }
                i = barriers.length;
                playlaser.isVisible = false;//laser disappears
            }
        }
        if(alienlaser.x>=you.x && alienlaser.x<=you.x +you.w && alienlaser.y>=you.y && alienlaser.y<=you.y +you.h)//if laser hits player...
        {
            lives -= 1;//you lose a life
            score -= 500;//you lose 500 points
            ingame = false;//game pauses
            you.x = 250;
            you.y = 450;//your position resets
            alienlaser.isVisible = false;//laser disappears
        }
        if(lives == 0)//if you lose all your lives...
        {
            winner = false;//you lost
            ingame = false;//game ends
            play = true;//you played the game
        }
        if(you.y <= aliens[29].y)//if the aliens reach you...
        {
            winner = false;//you lost
            ingame = false;//game ends
            play = true;//you played the game
        }
        for(int i=0; i<aliens.length; i++)
        {
            if(i == aliens.length -1)
            {
                if(showAlien[i]==false)//if last alien is off...
                {
                    ingame = false;//the game isn't on
                    winner = true;//did win
                    play = true;//have played the game
                }
                else//if last alien still appears...
                {
                    i = aliens.length;//nothing happens on screen
                }
            }
            if(showAlien[i]== true)//if aliens still appears...
            {
                i = aliens.length;//nothing happens on screen
            }
        }
    }

    private class TAdapter extends KeyAdapter 
    {

        public void keyReleased(KeyEvent e) 
        {
         int key = e.getKeyCode();
         playerRight = false;//turns off right movement
         playerLeft = false;//turns off left movement
        }

        public void keyPressed(KeyEvent e) 
        {
            //System.out.println( e.getKeyCode());
            // message = "Key Pressed: " + e.getKeyCode();
            int key = e.getKeyCode();
            if(key==39)//if right key...
            {
                playerRight = true;//allows right movement
            }
            if(key==37)//if left key...
            {
                playerLeft = true;//allows left movement
            }
            if(key == 32 && playlaser.isVisible == false)//if space bar and player laser is off...
            {
                playlaser.isFired = true;//player laser is turned on
            }
        }

    }




        public void mousePressed(MouseEvent e) 
        {
         if(play==false)//if game hasnt been played before...
         {
             int x = e.getX();
             int y = e.getY();
             ingame = true;//starts game
             message = "Lives: " + lives + " / Score: " + score;//creates message
         }
        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

        public void mouseClicked(MouseEvent e) 
        {   
        
        }

        public void run() 
        {

            long beforeTime, timeDiff, sleep;

            beforeTime = System.currentTimeMillis();
            int animationDelay = 10;//control FPS of board
            long time = 
            System.currentTimeMillis();
            while (true) 
            {//infinite loop
                // spriteManager.update();
                repaint();
                try 
                {
                  time += animationDelay;
                  Thread.sleep(Math.max(0,time - 
                  System.currentTimeMillis()));
                }catch (InterruptedException e) 
                {
                  System.out.println(e);
                }//end catch
            }//end while loop

        


        }
    
}
}
