package com.thechallengers.psagame.Dummy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.thechallengers.psagame.Dummy.Objects.Crane;
import com.thechallengers.psagame.Dummy.Physics.Block;
import com.thechallengers.psagame.Dummy.Physics.Physics2;
import com.thechallengers.psagame.Dummy.Physics.PhysicsInputHandler;
import com.thechallengers.psagame.base_classes_and_interfaces.ScreenWorld;
import com.thechallengers.psagame.helpers.AssetLoader;

import static com.thechallengers.psagame.game.PSAGame.SHORT_EDGE;

/**
 * Created by Phung Tuan Hoang on 9/11/2017.
 */

public class SinglePlayerGameWorld implements ScreenWorld {
    private Body crane;
    float CRANE_SPEED = 8f;
    private Stage stage;
    private World world;
    public Array<Body> bodyArray = new Array<Body>();
    Physics2 physics_engine;

    //touchpad-related variables
    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin skin; //store the skin for user interface: touchpad and release button
    private Drawable touchBackground, touchKnob;

    //release button-related variables
    private TextButton releaseButton;
    private TextButton.TextButtonStyle releaseStyle;
    private Drawable releaseBG;

    public SinglePlayerGameWorld(Physics2 physics_engine) {
        createUI();
        stage = new Stage();
        stage.addActor(touchpad);
        stage.addActor(releaseButton);

        this.physics_engine = physics_engine;
        crane = physics_engine.crane;
        world = physics_engine.getWorld();

        Gdx.input.setInputProcessor(stage);

    }

    public void updateCraneAction() {

        //control crane with touchpad
        crane.setLinearVelocity(CRANE_SPEED*touchpad.getKnobPercentX(), CRANE_SPEED*touchpad.getKnobPercentY());
        //release/grab containers

    }

    //create everthing considering user interface
    public void createUI() {
        //create the skin library for touchpad and release Button
        skin = new Skin();
        skin.add("touchBackground", new Texture("textures/touchBackground.png"));
        skin.add("touchKnob", new Texture("textures/touchKnob.png"));
        skin.add("releaseButton", new Texture("textures/releaseButton.png"));

        //create the style (from the skin) for touchpad
        touchpadStyle = new Touchpad.TouchpadStyle();
        touchBackground = skin.getDrawable("touchBackground");
        touchKnob = skin.getDrawable("touchKnob");
        //apply skin to style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        //create touchpad with style
        touchpad = new Touchpad(10, touchpadStyle); //minimum radius to move knob, style
        touchpad.setBounds(SHORT_EDGE-(15+200),15, 200, 200);

        releaseStyle = new TextButton.TextButtonStyle();
        releaseBG = skin.getDrawable("releaseButton");
        releaseStyle.up = releaseBG;
        releaseStyle.down = releaseBG;
        releaseStyle.font = AssetLoader.arial;
        releaseButton = new TextButton("", releaseStyle);
        releaseButton.setBounds(SHORT_EDGE-(15+450),15, 200, 200);
        addListenerToReleaseButton();
    }

    public void addListenerToReleaseButton() {
        releaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Array<Body> bodyArray = physics_engine.bodyArray;
                Body selectedBody = null;
                for (Body body: bodyArray) {
                    Fixture fixture = body.getFixtureList().get(0);
                    if (!(body.getUserData() instanceof Block)) {
                        continue;
                    }
                    if (fixture.testPoint(crane.getPosition().x, crane.getPosition().y)) {
                        selectedBody = body;
                        break;
                    }
                }
                if (selectedBody == null && physics_engine.cranedBody == null && physics_engine.isHoldingNext) {
                    physics_engine.createBlock(crane.getPosition().x, crane.getPosition().y);
                    physics_engine.isHoldingNext = false;
                }
                else if (selectedBody != null && physics_engine.cranedBody == null && !physics_engine.isHoldingNext) {
                    if (((Block) selectedBody.getUserData()).isCranable()) physics_engine.craneBody(selectedBody);
                }
                else if (physics_engine.cranedBody != null && !physics_engine.isHoldingNext) {
                    physics_engine.releaseCranedBody(crane.getPosition().x, crane.getPosition().y);
                }
                else if (selectedBody == null && physics_engine.cranedBody == null && !physics_engine.isHoldingNext) {
                    physics_engine.isHoldingNext = true;
                }


            }
        });
    }

    @Override
    public void update(float delta) {
        updateCraneAction();
        stage.act(delta);
        physics_engine.render();
        world.getBodies(bodyArray);
    }

    public Stage getStage() {
        return stage;
    }

    public World getWorld() {
        return world;
    }
}
