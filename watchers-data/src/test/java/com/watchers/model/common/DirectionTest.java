package com.watchers.model.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DirectionTest {

    @Test
    void resetPressures() {
        Direction direction = new Direction();
        direction.setXVelocity(10);
        direction.setYVelocity(-10);

        direction.resetPressures();

        assertThat(direction.getYDriftPressure(), equalTo(0L));
        assertThat(direction.getYDriftPressure(), equalTo(0L));
    }

    @Test
    void setVelocityFromPressureWithNoPressure() {
        Direction direction = new Direction();
        direction.setYVelocity(1);
        direction.setXVelocity(-2);

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(-2));
    }

    @Test
    void setVelocityFromPressureWithXPressure() {
        Direction direction = new Direction();
        direction.setYVelocity(-2);
        direction.setXVelocity(1);
        direction.setXDriftPressure(10);

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(-2));
        assertThat(direction.getXVelocity(), equalTo(1));

        direction.setYDriftPressure(10);
        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(-1));
        assertThat(direction.getXVelocity(), equalTo(1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(0));
        assertThat(direction.getXVelocity(), equalTo(1));


        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(1));
    }

    @Test
    void setVelocityFromPressureWithYPressure() {
        Direction direction = new Direction();
        direction.setYVelocity(1);
        direction.setXVelocity(-2);
        direction.setYDriftPressure(10);

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(-2));

        direction.setXDriftPressure(10);
        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(-1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(0));


        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(1));
        assertThat(direction.getXVelocity(), equalTo(1));
    }

    @Test
    void setVelocityFromPressureWithNegativeXPressure() {
        Direction direction = new Direction();
        direction.setYVelocity(-1);
        direction.setXVelocity(0);
        direction.setYDriftPressure(-10);

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(-1));
        assertThat(direction.getXVelocity(), equalTo(0));

        direction.setXDriftPressure(-10);
        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(-1));
        assertThat(direction.getXVelocity(), equalTo(-1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getYVelocity(), equalTo(-1));
        assertThat(direction.getXVelocity(), equalTo(-1));
    }

    @Test
    void setVelocityFromPressureWithNegativeYPressure() {
        Direction direction = new Direction();
        direction.setXVelocity(-1);
        direction.setYVelocity(0);
        direction.setXDriftPressure(-10);

        direction.setVelocityFromPressure(1);
        assertThat(direction.getXVelocity(), equalTo(-1));
        assertThat(direction.getYVelocity(), equalTo(0));

        direction.setYDriftPressure(-10);
        direction.setVelocityFromPressure(1);
        assertThat(direction.getXVelocity(), equalTo(-1));
        assertThat(direction.getYVelocity(), equalTo(-1));

        direction.setVelocityFromPressure(1);
        assertThat(direction.getXVelocity(), equalTo(-1));
        assertThat(direction.getYVelocity(), equalTo(-1));
    }

    @Test
    public void testAdjustPressureFromIncomingDirection(){
        Direction direction = new Direction(0,0);
        assertThat(direction.getXDriftPressure(), equalTo(0L));
        assertThat(direction.getYDriftPressure(), equalTo(0L));

        direction.adjustPressureFromIncomingDirection(new Direction(1,1));
        assertThat(direction.getXDriftPressure(), equalTo(1L));
        assertThat(direction.getYDriftPressure(), equalTo(1L));

        direction.adjustPressureFromIncomingDirection(new Direction(-1,-1));
        assertThat(direction.getXDriftPressure(), equalTo(0L));
        assertThat(direction.getYDriftPressure(), equalTo(0L));

        direction.adjustPressureFromIncomingDirection(new Direction(-3,-4));
        assertThat(direction.getXDriftPressure(), equalTo(-3L));
        assertThat(direction.getYDriftPressure(), equalTo(-4L));

        direction.adjustPressureFromIncomingDirection(new Direction(-10,14));
        assertThat(direction.getXDriftPressure(), equalTo(-13L));
        assertThat(direction.getYDriftPressure(), equalTo(10L));
    }
}