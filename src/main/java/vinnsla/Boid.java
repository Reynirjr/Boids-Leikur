package vinnsla;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * `Boid` klasinn lýsir einu staki í Boid herminum. Hvert Boid hefur staðsetningu,
 * hraða, mynd, og hegðun sem stjórnar hvernig það hreyfist og bregst við öðrum Boids og hákarli.
 */
public class Boid {
    private Vec2D stadsetning;  // Staðsetning Boid í herminum.
    private Vec2D hradi;  // Hraði Boid.
    private ImageView mynd;  // Mynd sem sýnir Boid í viðmótinu.
    private double paneBreidd;  // Breidd viðmótshluta þar sem Boid hreyfist.
    private double paneHaed;  // Hæð viðmótshluta þar sem Boid hreyfist.

    private static final double SKYNJUN = 50.0;  // Fjarlægð þar sem Boid getur skynjað önnur Boids.
    private static final double SEPERATION_DISTANCE = 25.0;  // Lágmarksfjarlægð milli Boids til að forðast árekstra.
    private static final double MAX_KRAFTUR = 0.05;  // Hámarks stýrikraftur Boid.
    private static final double MAX_HRADI = 1.5;  // Hámarkshraði Boid.

    private boolean flyja = false;  // Ef Boid er að flýja frá hákarli.

    /**
     * Setur flótta ástand Boid.
     *
     * @param flyja true ef Boid er að flýja, annars false.
     */
    public void setFlyja(boolean flyja) {
        this.flyja = flyja;
    }

    /**
     * Athugar hvort Boid sé að flýja.
     *
     * @return true ef Boid er að flýja, annars false.
     */
    public boolean isFlyja() {
        return flyja;
    }

    /**
     * Smiður fyrir Boid hlut.
     *
     * @param stadsetning Upphafsstaðsetning Boid.
     * @param hradi       Upphafshraði Boid.
     * @param filename    Slóð á myndina sem á að nota fyrir Boid.
     * @param paneBreidd  Breidd viðmótshluta þar sem Boid hreyfist.
     * @param paneHaed    Hæð viðmótshluta þar sem Boid hreyfist.
     */
    public Boid(Vec2D stadsetning, Vec2D hradi, String filename, double paneBreidd, double paneHaed) {
        this.stadsetning = stadsetning;
        this.hradi = hradi;
        this.paneBreidd = paneBreidd;
        this.paneHaed = paneHaed;
        Image image = new Image(getClass().getResourceAsStream(filename), 40, 40, true, true);
        this.mynd = new ImageView(image);

        this.mynd.setScaleX(-1);
        uppfaeraMynd();
    }

    /**
     * Stýrir Boid í átt að tilteknu markmiði.
     *
     * @param target Staðsetning markmiðsins.
     * @return Vec2D vektor sem stýrir Boid í átt að markmiði.
     */
    private Vec2D steerTowards(Vec2D target) {
        Vec2D desired = Vec2D.draga(target, this.stadsetning);
        desired.setMagnitude(MAX_HRADI);
        Vec2D steer = Vec2D.draga(desired, this.hradi);
        steer.limit(MAX_KRAFTUR);
        return steer;
    }

    /**
     * Uppfærir stöðu og hegðun Boid með tilliti til annarra Boids og hákarls.
     *
     * @param boids  Listi af öðrum Boids í herminum.
     * @param hakarl Hákarl í herminum.
     */
    public void update(List<Boid> boids, Hakarl hakarl) {
        fordast(hakarl);
        Vec2D adlaga = adlaga(boids);
        Vec2D sameining = sameina(boids);
        Vec2D sundrun = sundrun(boids);

        adlaga.sinnum(1.0);
        sameining.sinnum(1.0);
        sundrun.sinnum(2.0);

        hradi.baeta(adlaga);
        hradi.baeta(sameining);
        hradi.baeta(sundrun);
        hreyfa();
        uppfaeraMynd();
    }

    /**
     * Aðlögun Boid byggt á hraða annarra Boids í nágrenninu.
     *
     * @param boids Listi af Boids til að aðlaga sig að.
     * @return Vec2D vektor sem lýsir aðlögun.
     */
    private Vec2D adlaga(List<Boid> boids) {
        Vec2D styring = new Vec2D(0, 0);
        int total = 0;
        for (Boid other : boids) {
            double d = stadsetning.fjarlaegd(other.stadsetning);
            if (other != this && d < SKYNJUN) {
                styring.baeta(other.hradi);
                total++;
            }
        }
        if (total > 0) {
            styring.deilt(total);
            styring.setMagnitude(MAX_HRADI);
            styring.draga(this.hradi);
            styring.limit(MAX_KRAFTUR);
        }
        return styring;
    }

    /**
     * Samþætting Boid í átt að miðju hópsins.
     *
     * @param boids Listi af Boids til að sameinast með.
     * @return Vec2D vektor sem lýsir sameiningu.
     */
    private Vec2D sameina(List<Boid> boids) {
        Vec2D centerOfMass = new Vec2D(0, 0);
        int total = 0;
        for (Boid other : boids) {
            double d = stadsetning.fjarlaegd(other.stadsetning);
            if (other != this && d < SKYNJUN) {
                centerOfMass.baeta(other.stadsetning);
                total++;
            }
        }
        if (total > 0) {
            centerOfMass.deilt(total);
            return steerTowards(centerOfMass);
        }
        return centerOfMass;
    }

    /**
     * Sundrun Boid frá öðrum Boids til að forðast árekstra.
     *
     * @param boids Listi af Boids til að sundrast frá.
     * @return Vec2D vektor sem lýsir sundrun.
     */
    private Vec2D sundrun(List<Boid> boids) {
        Vec2D styring = new Vec2D(0, 0);
        int total = 0;
        for (Boid other : boids) {
            double fjarlaegd = stadsetning.fjarlaegd(other.stadsetning);
            if (other != this && fjarlaegd < SEPERATION_DISTANCE && fjarlaegd > 0) {
                Vec2D diff = Vec2D.draga(this.stadsetning, other.stadsetning);
                diff.deilt(fjarlaegd);
                styring.baeta(diff);
                total++;
            }
        }
        if (total > 0) {
            styring.deilt(total);
        }
        styring.limit(MAX_KRAFTUR);
        return styring;
    }

    /**
     * Forðast hákarl með því að breyta hraða Boid.
     *
     * @param hakarl Hákarl í herminum sem Boid forðast.
     */
    public void fordast(Hakarl hakarl) {
        Vec2D hakarlStadsetning = hakarl.getStadsetning();
        double fjarlaegd = stadsetning.fjarlaegd(hakarlStadsetning);
        if (fjarlaegd < 100 * hakarl.getStaekkunFasti()) {
            setFlyja(true);
            Vec2D fraHakarli = Vec2D.draga(stadsetning, hakarlStadsetning);
            fraHakarli.normalize();
            fraHakarli.deilt(fjarlaegd);
            fraHakarli.sinnum(10);
            hradi.baeta(fraHakarli);
        } else {
            setFlyja(false);
        }
    }

    /**
     * Hreyfir Boid með því að bæta við hraða og uppfæra staðsetningu.
     */
    public void hreyfa() {
        stadsetning.baeta(hradi);
        limitVelocity();

        // Endurræsing Boid ef það fer út fyrir mörk.
        if (stadsetning.x > paneBreidd) stadsetning.x = 0;
        if (stadsetning.x < 0) stadsetning.x = paneBreidd;
        if (stadsetning.y > paneHaed) stadsetning.y = 0;
        if (stadsetning.y < 0) stadsetning.y = paneHaed;

        uppfaeraMynd();
    }

    /**
     * Takmarkar hraða Boid eftir því hvort það er að flýja eða ekki.
     */
    private void limitVelocity() {
        double maxSpeed = isFlyja() ? 5.0 : 2.0;
        if (hradi.magnitude() > maxSpeed) {
            hradi.setMagnitude(maxSpeed);
        }
    }

    /**
     * Skilar staðsetningu Boid.
     *
     * @return Vec2D staðsetning Boid.
     */
    public Vec2D getStadsetning() {
        return this.stadsetning;
    }

    /**
     * Uppfærir mynd Boid með nýrri staðsetningu og snýr henni eftir hreyfistefnu.
     */
    public void uppfaeraMynd() {
        mynd.setX(stadsetning.x - mynd.getFitWidth() / 2);
        mynd.setY(stadsetning.y - mynd.getFitHeight() / 2);
        snuaMyndinni();

        if (hradi.y > 0) {
            mynd.setScaleY(-1);
        } else {
            mynd.setScaleY(1);
        }
    }

    /**
     * Breytir mynd og stærð Boid.
     *
     * @param mynd   Slóð að nýrri mynd fyrir Boid.
     * @param staerd Stærð myndar Boid.
     */
    public void bennar(String mynd, double staerd) {
        Image nyMynd = new Image(getClass().getResourceAsStream(mynd), staerd, staerd, true, true);
        this.mynd.setImage(nyMynd);
        uppfaeraMynd();
    }

    /**
     * Snýr mynd Boid eftir hreyfistefnu.
     */
    private void snuaMyndinni() {
        double angle = Math.toDegrees(Math.atan2(hradi.y, hradi.x));
        mynd.setRotate(angle);
    }

    /**
     * Skilar ImageView hlut sem lýsir mynd Boid.
     *
     * @return ImageView sem inniheldur mynd Boid.
     */
    public ImageView getImageView() {
        return mynd;
    }
}
