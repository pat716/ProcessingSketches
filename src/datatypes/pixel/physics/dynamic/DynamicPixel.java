package datatypes.pixel.physics.dynamic;

import datatypes.pixel.CustomPixel;
import datatypes.pixel.physics.PhysicsManager;
import datatypes.pixel.physics.PhysicsPixel;
import datatypes.pr.Region;
import math.Vector;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import sketches.FallingSand;

import java.awt.geom.Rectangle2D;
import java.util.*;

import static java.lang.Math.PI;
import static processing.core.PConstants.TWO_PI;

/**
 * Created by psweeney on 9/24/16.
 */
public abstract class DynamicPixel extends PhysicsPixel {
    private static final float MIN_NEW_VELOCITY_AMT = 0.0375f;
    private static final float COLLISION_VSPREAD_MULTIPLIER = 0.025f;
    private static final float PRESSURE_VECTOR_MULTIPLIER = .00005f;
    private static final float RANDOM_MOVE_CHANCE_MULTIPLIER = 0.00025f;
    private static final float MAX_SWAP_SEARCH_ANGLE = 17 * (PConstants.PI/32);
    private static final boolean RANDOM_MOVE_ENABLED = false;
    private static final boolean PRESSURE_VECTOR_ENABLED = false;
    private static final boolean VSWAP_ENABLED = true;
    private static final boolean VSWAP_IF_SAME_TYPE = false;

    private float vx, vy;
    private float lastMoveAmount;

    private static final Set<PhysicsPixelType> dynamicTypes = new HashSet<>(Arrays.asList(new PhysicsPixelType[] {
            PhysicsPixelType.ASH,
            PhysicsPixelType.CO2_GAS,
            PhysicsPixelType.FIRE,
            PhysicsPixelType.GUNPOWDER,
            PhysicsPixelType.LAVA,
            PhysicsPixelType.STEAM,
            PhysicsPixelType.URANIUM,
            PhysicsPixelType.WATER
    }));

    public DynamicPixel(PhysicsManager manager, float x, float y, PhysicsPixelType type){
        super(manager, x, y, type);
        vx = 0;
        vy = 0;
        lastMoveAmount = 0;
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public float getLastMoveAmount() {
        return lastMoveAmount;
    }

    public static Set<PhysicsPixelType> getAllDynamicTypes(){
        return dynamicTypes;
    }

    public boolean swapCompatible(PhysicsPixel other){
        if(other == null) return true;

        if(getType() == PhysicsPixelType.STEAM && other.getType() == PhysicsPixelType.STEAM) return true;
        if(getType() == PhysicsPixelType.GUNPOWDER && other.getType() == PhysicsPixelType.GUNPOWDER) return true;

        if((getType() == PhysicsPixelType.WATER && other.getType() == PhysicsPixelType.STEAM) || (getType() ==
                PhysicsPixelType.STEAM && other.getType() == PhysicsPixelType.WATER)) return true;


        if(getType() != other.getType() && other instanceof DynamicPixel){
            DynamicPixel dOther = (DynamicPixel) other;

            if(getDensity() > dOther.getDensity() && Math.random() < 1f - (dOther.getDensity()/getDensity())) return true;
        }

        return false;
    }

    public void changeVelocityByAngle(float randomAmount){
        Vector randomVector = Vector.getVectorRelativeToOrigin((float) Math.random() * TWO_PI, randomAmount);
        vx += randomVector.x;
        vy += randomVector.y;
        /*
        float velocityAngle = velocityVector.getAngleFromOrigin(), velocityDistance = velocityVector.getMagnitude();
        float velocityRandomAmount = (float) (Math.random() * PI/2) * randomAmount;
        if(Math.random() <= 0.5){
            velocityRandomAmount *= -1;
        }

        velocityAngle += velocityRandomAmount;
        velocityVector = Vector.getVectorRelativeToOrigin(velocityAngle, velocityDistance);
        vx = velocityVector.x;
        vy = velocityVector.y; */
    }

    private Vector getPressureVector(){
        float pressureX = 0, pressureY = 0;
        for(Map.Entry<Vector, PhysicsPixel> entry : getSurroundingPixels().entrySet()){
            Vector key = entry.getKey();
            PhysicsPixel value = entry.getValue();
            if(key == null || value == null) continue;
            Region.RelativeQuadrant quadrant = Region.getRelativeQuadrant(getX(), getY(), key.x, key.y);
            float density = 0;
            if(value instanceof DynamicPixel) density = ((DynamicPixel) value).getDensity();
            else density = PhysicsPixel.DEFAULT_STATIC_DENSITY;

            switch (quadrant){
                case LX_LY:
                    pressureX += density / (2 * FallingSand.sqrt2);
                    pressureY += density / (2 * FallingSand.sqrt2);
                    break;
                case LX_UY:
                    pressureX += density / (2 * FallingSand.sqrt2);
                    pressureY -= density / (2 * FallingSand.sqrt2);
                    break;
                case UX_LY:
                    pressureX -= density / (2 * FallingSand.sqrt2);
                    pressureY += density / (2 * FallingSand.sqrt2);
                    break;
                case UX_UY:
                    pressureX -= density / (2 * FallingSand.sqrt2);
                    pressureY -= density / (2 * FallingSand.sqrt2);
                    break;
                case LX_CY:
                    pressureX += density;
                    break;
                case CX_LY:
                    pressureY += density;
                    break;
                case CX_UY:
                    pressureY -= density;
                    break;
                case UX_CY:
                    pressureX -= density;
                    break;
            }
        }
        return new Vector(pressureX * PRESSURE_VECTOR_MULTIPLIER, pressureY * PRESSURE_VECTOR_MULTIPLIER);
    }

    public void vSwap(PhysicsPixel p){
        if(!VSWAP_ENABLED || p == null) return;
        if(!(p instanceof DynamicPixel)){
            vx *= getFluidity();
            vy *= getFluidity();
            return;
        }

        if(p.getType() == getType() && !VSWAP_IF_SAME_TYPE) return;
        DynamicPixel d = (DynamicPixel) p;
        vx += d.vx * (1f - getFluidity()) * (1f - d.getFluidity()) * COLLISION_VSPREAD_MULTIPLIER;
        vy += d.vy * (1f - getFluidity()) * (1f - d.getFluidity()) * COLLISION_VSPREAD_MULTIPLIER;
    }

    @Override
    public void update() {
        Map<Vector, PhysicsPixel> surrounding = getSurroundingPixels();
        float randomVelocityAmount = getRandomVelocityAmount();
        if(lastMoveAmount < 1 && randomVelocityAmount > 0){
            randomVelocityAmount *= 2;
        }
        changeVelocityByAngle(randomVelocityAmount);
        float densityMultiplier = 1f/(PApplet.sq(1f + getDensity()));

        Vector gravityVector = getManager().getGravityVector();
        if(gravityVector.getMagnitude() == 0){
            gravityVector = Vector.getVectorRelativeToOrigin((float) Math.random() * TWO_PI, 1f - getFluidity());
        }

        vx += gravityVector.x * getGravityMultiplier();
        vy += gravityVector.y * getGravityMultiplier();

        if(PRESSURE_VECTOR_ENABLED){
            PVector pressureVector = getPressureVector().mult(
                    (1/getDensity()) * (1 - getFluidity() * 0.5f) * (1/PApplet.max(1, lastMoveAmount)));
            vx += pressureVector.x;
            vy += pressureVector.y;
        }

        float velocityDistance = PApplet.dist(0, 0, vx, vy);

        float x = getX(), y = getY(), newX = x, newY = y, newDist = 0;
        Vector velocityVector = new Vector(vx, vy);
        float velocityAngle = velocityVector.getAngleFromOrigin();

        float maxVelocityAngleChange = (MAX_SWAP_SEARCH_ANGLE) * getFluidity();
        //float maxVelocityAngleChange = (float) (TWO_PI) * getFluidity();

        float randomMoveChance = 0;
        for(PhysicsPixel p : surrounding.values()){
            if(p != null && p.getType() == getType()){
                randomMoveChance += getFluidity() / (8 * PApplet.max(1, PApplet.dist(getX(), getY(), p.getX(),
                        p.getY())));
            }
        }

        for(float angleOffset = 0; angleOffset < maxVelocityAngleChange; angleOffset += PI/8f){
            if(angleOffset == 0){
                for(float i = 0; i < velocityDistance; i++){
                    float distRatio = i/velocityDistance;
                    float currX = x + vx * distRatio, currY = y + vy * distRatio;
                    PhysicsPixel currPixel = getManager().getPixel(currX, currY);
                    if(currPixel == null || swapCompatible(currPixel)){
                        if(i > newDist){
                            newX = currX;
                            newY = currY;
                            newDist = i;
                        }
                    } else {
                        vSwap(currPixel);
                        if(currPixel.getType() != getType()){
                            break;
                        }
                    }
                }
                continue;
            }

            float angleRatio = angleOffset/maxVelocityAngleChange;
            float maxDistance = velocityDistance;// * (1 - angleRatio);

            if(maxDistance <= newDist){
                break;
            }

            float leftVelocityAngle = velocityAngle - angleOffset, rightVelocityAngle = velocityAngle + angleOffset;
            Vector leftVelocityVector = Vector.getVectorRelativeToOrigin(leftVelocityAngle, maxDistance),
                    rightVelocityVector = Vector.getVectorRelativeToOrigin(rightVelocityAngle, maxDistance);
            float leftVX = leftVelocityVector.x, leftVY = leftVelocityVector.y, rightVX = rightVelocityVector.x,
                    rightVY = rightVelocityVector.y;

            if(PApplet.dist(leftVX, leftVY, rightVX, rightVY) < 1f) continue;

            boolean leftCollision = false, rightCollision = false;

            for(float i = 1; i < maxDistance; i++) {
                if(leftCollision && rightCollision) break;
                float distRatio = i/maxDistance;

                float leftX = x + leftVX * distRatio, leftY = y + leftVY * distRatio, rightX = x + rightVX * distRatio,
                        rightY = y + rightVY * distRatio;

                if(PApplet.dist(leftX, leftY, rightX, rightY) < 1f){
                    continue;
                }

                PhysicsPixel leftPixel = getManager().getPixel(leftX, leftY), rightPixel =
                        getManager().getPixel(rightX, rightY);

                if(leftCollision){
                    if(rightPixel == null || swapCompatible(rightPixel)){
                        if(i > newDist){
                            newX = rightX;
                            newY = rightY;
                            newDist = i;
                        }
                    } else {
                        vSwap(rightPixel);

                        if(rightPixel.getType() != getType()){
                            break;
                        }
                    }
                } else if(rightCollision){
                    if(leftPixel == null || swapCompatible(leftPixel)){
                        if(i > newDist){
                            newX = leftX;
                            newY = leftY;
                            newDist = i;
                        }
                    } else {
                        vSwap(leftPixel);
                        if(leftPixel.getType() != getType()){
                            break;
                        }
                    }
                } else {
                    if((leftPixel == null || swapCompatible(leftPixel)) && (rightPixel == null ||
                            swapCompatible(rightPixel))){
                        if(i > newDist) {
                            newDist = i;
                            if (Math.random() <= 0.5f) {
                                newX = leftX;
                                newY = leftY;
                            } else {
                                newX = rightX;
                                newY = rightY;
                            }
                        }
                    } else if(leftPixel == null || swapCompatible(leftPixel)){
                        if(i > newDist){
                            newX = leftX;
                            newY = leftY;
                            newDist = i;
                        }

                        if(rightPixel != null){
                            vSwap(rightPixel);
                            if(rightPixel.getType() != getType())
                                rightCollision = true;
                        }
                    } else if(rightPixel == null || swapCompatible(rightPixel)){
                        if(i > newDist){
                            newX = rightX;
                            newY = rightY;
                            newDist = i;
                        }

                        if(leftPixel != null){
                            vSwap(leftPixel);
                            if(leftPixel.getType() != getType())
                                leftCollision = true;
                        }
                    } else {
                        vSwap(leftPixel);
                        vSwap(rightPixel);

                        if(leftPixel.getType() != getType()){
                            leftCollision = true;
                        }
                        if(rightPixel.getType() != getType()){
                            rightCollision = true;
                        }
                    }
                }
            }
        }

        float newVelocityAmt = PApplet.max(1f - getFluidity(), MIN_NEW_VELOCITY_AMT);

        float prevX = getX(), prevY = getY();

        if(PApplet.dist(prevX, prevY, newX, newY) < 1 && RANDOM_MOVE_ENABLED){
            for(Map.Entry<Vector, PhysicsPixel> entry : surrounding.entrySet()){
                if((entry.getValue() == null || swapCompatible(entry.getValue())) && Math.random() < randomMoveChance * RANDOM_MOVE_CHANCE_MULTIPLIER){
                    newX = entry.getKey().x;
                    newY = entry.getKey().y;
                }
            }
        }



        vx *= 1f - newVelocityAmt;
        vy *= 1f - newVelocityAmt;

        vx = vx * (1f - newVelocityAmt) + (newX - prevX) * newVelocityAmt;
        vy = vy * (1f - newVelocityAmt) + (newY - prevY) * newVelocityAmt;

        lastMoveAmount = PApplet.dist(prevX, prevY, newX, newY);

        getManager().swapPixels(prevX, prevY, newX, newY);
    }

    public abstract float getDensity();
    public abstract float getFluidity();
    public abstract float getGravityMultiplier();
    public abstract float getRandomVelocityAmount();
}
