package com.icefill.game.actors.devices;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.icefill.game.Assets;
import com.icefill.game.Global;
import com.icefill.game.actors.dungeon.AreaCell;
import com.icefill.game.actors.dungeon.DungeonGroup;
import com.icefill.game.actors.EquipActor;
import com.icefill.game.actors.ObjActor;
import com.icefill.game.actors.dungeon.RoomGroup;
import com.icefill.game.extendedActions.ExtendedActions;

public class ItemActor extends DeviceActor {
    EquipActor equip;
    Texture shadow;
    TextureRegion background = new TextureRegion(Assets.getAsset("sprite/icon.atlas", TextureAtlas.class).findRegion("scroll"));

    //	float rotation;
    public ItemActor(EquipActor equip, AreaCell cell, RoomGroup room) {
        super(room);
        curr_dir = DIR.DL;
        this.equip = equip;
        //setRotation(Randomizer.next(30,150));
        setX(cell.getX());
        setY(cell.getY());
        setZ(7);

        SequenceAction seq = new SequenceAction();
        seq.addAction(ExtendedActions.moveTo3D(getX(), getY(), getZ() + 5, .3f));
        seq.addAction(ExtendedActions.moveTo3D(getX(), getY(), getZ(), .3f));
        this.addAction(Actions.sequence(
                ExtendedActions.moveToParabolic(getX(), getY(), getZ(), .3f),
                Actions.forever(seq)
                )
        );

        harzardeous = false;
        shadow = Assets.getAsset("sprite/shadow.png", Texture.class);
    }


    public void draw(Batch batch, float delta) {
        super.draw(batch, delta);
        batch.setColor(1f, 1f, 1f, 0.6f);
        batch.draw(shadow, getX() - 16, getY() - 8);
        batch.setColor(1f, 1f, 1f, 1f);
        if (equip.type_for_ability.equals("scroll")) {
            batch.draw(background,getX()-16,getY()-16+getZ());
        }
        equip.getSprites().drawAnimationMiddleRotation(batch, delta, 0, DIR.DL, getX(), getY() + getZ(), getRotation(), 1f, 1f);

    }

    public void action(final DungeonGroup dungeon, final AreaCell target_cell) {
        final AreaCell cell = dungeon.getCurrentRoom().getCell(dungeon.getSelectedObj());
        if (dungeon.getCurrentRoom().getObj(target_cell) != null) {
            ObjActor obj = dungeon.getCurrentRoom().getObj(target_cell);
            if (obj.getTeam() == 0) {
                if (Global.getPlayerTeam().getInventory().setSlot(equip)) {
                    this.addAction(Actions.sequence(ExtendedActions.moveTo3D(getX(), getY(), getZ() + 15f, .7f),
                            Actions.run(new Runnable() {
                                public void run() {
                                    cell.device = null;
                                    self.remove();
                                }
                            }))
                    );
                    Sound hit_sound = Assets.getAsset("sound/item.wav", Sound.class);
                    hit_sound.play();
                } else {
                    Global.showMessage("Inventory is full!!", 0);
                }
            } else {
                this.addAction(Actions.sequence(ExtendedActions.moveTo3D(getX(), getY(), getZ() + 15f, .7f),
                        Actions.run(new Runnable() {
                            public void run() {
                                cell.device = null;
                                self.remove();
                            }
                        }))
                );
                Sound hit_sound = Assets.getAsset("sound/item.wav", Sound.class);
                hit_sound.play();

            }
        }
        //dungeon.getCurrentRoom().getMap().getAreaCell(dungeon.getSelectedObj().getXX(),dungeon.getSelectedObj().getYY()).device=null;
    }


}
