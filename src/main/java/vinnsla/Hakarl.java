package vinnsla;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * `Hakarl` klasinn lýsir hákarli í Boid herminum. Hann hefur staðsetningu,
 * hraða, stærð, og mynd sem er sýnd í viðmótinu. Hákarlinn getur hreyft sig,
 * stækkað, og skipt um mynd eftir atvikum.
 */
public class Hakarl {
    private Vec2D stadsetning;  // Staðsetning hákarls í herminum.
    private Vec2D hradi;  // Hraði hákarls.
    private ImageView mynd;  // Mynd sem sýnir hákarl í viðmótinu.
    private double paneBreidd;  // Breidd viðmótshluta þar sem hákarl hreyfist.
    private double paneHaed;  // Hæð viðmótshluta þar sem hákarl hreyfist.
    private DoubleProperty staerd = new SimpleDoubleProperty(60);  // Stærð hákarls.
    private static final double STAEKKUN_FASTI = 1.1;  // Fasti til að stækka hákarl.
    private static final double AT_RADIUS = 30;  // Radíus þar sem hákarl getur ráðist á Boid.
    private double atFjarlaegd;  // Fjarlægð þar sem hákarl getur ráðist á Boid.

    /**
     * Smiður fyrir Hakarl hlut.
     *
     * @param stadsetning Upphafsstaðsetning hákarls.
     * @param myndNafn    Slóð á myndina sem á að nota fyrir hákarl.
     * @param paneBreidd  Breidd viðmótshluta þar sem hákarl hreyfist.
     * @param paneHaed    Hæð viðmótshluta þar sem hákarl hreyfist.
     */
    public Hakarl(Vec2D stadsetning, String myndNafn, double paneBreidd, double paneHaed) {
        this.stadsetning = stadsetning;
        this.hradi = new Vec2D(0, 0);
        this.paneBreidd = paneBreidd;
        this.paneHaed = paneHaed;
        this.atFjarlaegd = AT_RADIUS;
        Image mynd = new Image(getClass().getResourceAsStream(myndNafn), staerd.get(), staerd.get(), true, true);
        this.mynd = new ImageView(mynd);
        uppfaeraImageView();
    }

    /**
     * Hreyfir hákarl með því að bæta við hraða og uppfæra staðsetningu.
     * Ef hákarl fer út fyrir mörk, þá fer hann aftur á byrjunarstað.
     */
    public void hreyfa() {
        stadsetning.baeta(hradi);
        wrapAround();
        uppfaeraImageView();
    }

    /**
     * Gerir það að verkum að hákarl birtist aftur á móti ef hann fer út fyrir mörk.
     */
    private void wrapAround() {
        if (stadsetning.x > paneBreidd) stadsetning.x = 0;
        if (stadsetning.x < 0) stadsetning.x = paneBreidd;
        if (stadsetning.y > paneHaed) stadsetning.y = 0;
        if (stadsetning.y < 0) stadsetning.y = paneHaed;
    }

    /**
     * Uppfærir staðsetningu og stefnu myndarinnar sem sýnir hákarl í viðmótinu.
     */
    public void uppfaeraImageView() {
        mynd.setX(stadsetning.x - mynd.getFitWidth() / 2);
        mynd.setY(stadsetning.y - mynd.getFitHeight() / 2);

        if (hradi.magnitude() > 0) {
            if (hradi.y == 0) {
                if (hradi.x < 0) {
                    // hreyfa sig til vinstri
                    mynd.setScaleX(-1);  // snýr myndinni
                    mynd.setRotate(0);
                } else {
                    mynd.setScaleX(1);
                    mynd.setRotate(0);
                }
            } else {
                double angle = Math.toDegrees(Math.atan2(hradi.y, Math.abs(hradi.x)));
                mynd.setRotate(hradi.x < 0 ? -angle : angle);
                mynd.setScaleX(hradi.x < 0 ? -1 : 1); // snýr myndinni ef farið er til vinstri
            }
        } else {
            mynd.setRotate(0);
            mynd.setScaleX(1);
        }
    }

    /**
     * Skilar ImageView hlutnum sem inniheldur mynd hákarls.
     *
     * @return ImageView sem sýnir mynd hákarls.
     */
    public ImageView getMynd() {
        return mynd;
    }

    /**
     * Stillir nýjan hraða fyrir hákarl.
     *
     * @param nyrHradi Nýr hraði hákarls.
     */
    public void setHradi(Vec2D nyrHradi) {
        this.hradi = nyrHradi;
    }

    /**
     * Skilar hraða hákarls.
     *
     * @return Vec2D sem lýsir hraða hákarls.
     */
    public Vec2D getHradi() {
        return hradi;
    }

    /**
     * Skilar staðsetningu hákarls.
     *
     * @return Vec2D sem lýsir staðsetningu hákarls.
     */
    public Vec2D getStadsetning() {
        return stadsetning;
    }

    /**
     * Stækkar hákarl um fyrirfram ákveðinn fasti og uppfærir myndina.
     *
     * @param mynd Slóð á nýja mynd fyrir hákarl eftir stækkun.
     */
    public void staekkun(String mynd) {
        double nyStaerd = staerd.get() * STAEKKUN_FASTI;
        staerd.set(nyStaerd);
        atFjarlaegd *= STAEKKUN_FASTI;
        Image nyMynd = new Image(getClass().getResourceAsStream(mynd), nyStaerd, nyStaerd, true, true);
        this.mynd.setImage(nyMynd);
        uppfaeraImageView();
    }

    /**
     * Uppfærir mynd hákarls í nýja mynd (t.d. þegar Jaws hamur er virkjaður).
     *
     * @param mynd Slóð á nýja mynd fyrir hákarl.
     */
    public void jaws(String mynd) {
        Image nyMynd = new Image(getClass().getResourceAsStream(mynd), staerd.get(), staerd.get(), true, true);
        this.mynd.setImage(nyMynd);
        uppfaeraImageView();
    }

    /**
     * Skilar ráðast fjarlægð hákarls, sem lýsir þeirri fjarlægð sem hákarl getur ráðist á Boid.
     *
     * @return Ráðast fjarlægð hákarls.
     */
    public double getAtFjarlaegd() {
        return atFjarlaegd;
    }

    /**
     * Skilar stækkunarfasta hákarls.
     *
     * @return Stækkunarfasti hákarls.
     */
    public double getStaekkunFasti() {
        return STAEKKUN_FASTI;
    }
}
