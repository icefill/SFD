package com.icefill.game.actors;


import com.badlogic.gdx.Gdx;
import com.icefill.game.*;


import com.icefill.game.Job.EquipmentForLevel;
import com.icefill.game.actors.actionActors.Function;
import com.icefill.game.actors.devices.GoldActor;
import com.icefill.game.actors.dungeon.AreaCell;
import com.icefill.game.actors.dungeon.DungeonGroup;
import com.icefill.game.actors.dungeon.RoomGroup;
import com.icefill.game.actors.windows.LevelUpWindow;
import com.icefill.game.actors.windows.PersonalInventory;
import com.icefill.game.actors.windows.ObjInfoWindow;
import com.icefill.game.actors.windows.TargetInfoActor;
import com.icefill.game.utils.Randomizer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import com.icefill.game.actors.actionActors.AbilityActor;
import com.icefill.game.actors.actionActors.ActionActor;
import com.icefill.game.actors.actionActors.ActionActor.ActionContainer;
import com.icefill.game.actors.actionActors.ObjActions;
import com.icefill.game.actors.devices.ItemActor;
import com.icefill.game.extendedActions.ExtendedActions;
import com.icefill.game.sprites.NonObjSprites;
import com.icefill.game.sprites.ObjSprites;

public class ObjActor extends BasicActor implements Constants {
    //internal vars
    protected int curr_ani;
    private boolean is_position_changed;
    private boolean is_summoned;
    private int idle_animation = IDLE;
    public int obj_state = PL_DONE;
    public int obj_condition;// poisoned paralized burned frozen normal
    public int temp_exp;
    public int fire_level;
    public int lightning_level;
    public int holy_level;
    public int unholy_level;
    private int runaway_counter;

    private int status_change_label_index = 0;

    CharUI char_ui;
    private String type;
    private LinkedList<String> forbidden_action_type_list;
    private boolean unique_sprite;
    private boolean is_leader;
    private int team;
    private boolean obs_destroy_flag;
    public PersonalInventory inventory;
    com.icefill.game.actors.windows.ObjInfoWindow skill_window;
    com.icefill.game.actors.windows.LevelUpWindow level_up_window;

    NonObjSprites glow;

    public float speed;
    public Job job;
    //Color adjust_c;


    private boolean dodging = false;
    private int dodge_direction = 1;

    private boolean action_selected = false;
    public boolean selected = false;
    public boolean is_glowing = false;
    protected boolean hide_shadow = false;
    private int controlled = CONTROLLED_PLAYER;

    private float backup_pos_x;
    private float backup_pos_y;
    private float backup_pos_z;

    //char status
    private float mv_spd;

    public int level = 1;
    public int experience = 0;


    public int dodge;

    public TotalStatus status;

    public LinkedList<ActionContainer> ability_list;
    public LinkedList<AbilityActor> passive_action_list;
    public LinkedList<AbilityActor> attain_ability_list;
    public LinkedList<AbilityActor> attain_passive_ability_list;

    public LinkedList<StatusTuple.TURNEFFECT> turn_effect_list;

    public ActionContainer move_ability;
    public com.icefill.game.actors.actionActors.Function dead_ability;

    //for animation
    public boolean equippable = false;

    private ActionContainer selected_action;
    private int selected_equip = -1;

    private Label turn_effect_info;
    private com.icefill.game.actors.windows.TargetInfoActor target_info;
    private com.icefill.game.actors.windows.TargetInfoActor ability_info;

    public ObjActor(int xx, int yy, int team, int level, Job job, int controlled) {
        this.team = team;
        this.level = level;
        this.controlled = controlled;
        initialize(job);
        setXX(xx);
        setYY(yy);
        if (ability_list == null) ability_list = new LinkedList<ActionContainer>();


        ability_list.add(new ActionContainer(Assets.actions_map.get("OpenInventory"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("OpenMap"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("AddAbility"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("Wait"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("SetDirection"), 0));


    }

    public ObjActor(String json_name, DragAndDrop drag) {

        ability_list = new LinkedList<ActionContainer>();
        passive_action_list = new LinkedList<AbilityActor>();
        Json json = new Json();
        ObjActor.Seed temp_factory = json.fromJson(ObjActor.Seed.class
                , Gdx.files.internal("objs_data/chars/" + json_name + ".json"));
        initialize(Assets.jobs_map.get(temp_factory.job));


        ability_list.add(new ActionContainer(Assets.actions_map.get("OpenInventory"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("OpenMap"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("AddAbility"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("Wait"), 0));
        ability_list.add(new ActionContainer(Assets.actions_map.get("SetDirection"), 0));

        this.sprites = Assets.obj_sprites_map.get(temp_factory.sprites_name);
        this.sprites_name = new String(temp_factory.sprites_name);

        inventory = new PersonalInventory(Assets.getSkin(), temp_factory.equipment_name, temp_factory.item_name, this, drag);

        if (temp_factory.base_status != null) {
            status = new TotalStatus(this, temp_factory.base_status, inventory);
        }

        status.setStatus(inventory, turn_effect_list);
        status.current_hp = status.total_status.HP;
        inventory.renewStatus();
    }


    public void setLeader() {
        is_leader = true;
    }

    public boolean isLeader() {
        return is_leader;
    }

    public void setUniqueSprite(boolean flag) {
        unique_sprite = flag;
    }

    public PersonalInventory getInventory() {
        return inventory;
    }

    public void concileShadow() {
        hide();
        hide_shadow = true;
    }

    public void revealShadow() {
        hide_shadow = false;
    }

    public void initialize(Job job) {

        this.job = job;
        Label.LabelStyle style = new Label.LabelStyle(Assets.getFont(), Color.BLACK);
        style.background = new NinePatchDrawable(new NinePatch(Assets.getAsset(("sprite/message_background.png"), Texture.class), 15, 8, 8, 15));

        turn_effect_info = new Label("Bummer", new Label.LabelStyle(Assets.getFont(), Color.GREEN));
        turn_effect_info.setPosition(-20, 36 + getZ());
        turn_effect_info.setVisible(false);
        turn_effect_info.pack();

        target_info = new com.icefill.game.actors.windows.TargetInfoActor(this, style);
        ability_info = new TargetInfoActor(this, style);


        skill_window = new com.icefill.game.actors.windows.ObjInfoWindow(this, Assets.getSkin());


        setJob(job, true);

        elapsed_time = (float) (Math.random() * 10);
        self = this;
        this.obj_state = PL_DONE;
        //this.job_name=job.job_name;
        char_ui = new CharUI(team, this);


        turn_effect_list = new LinkedList<StatusTuple.TURNEFFECT>();
        forbidden_action_type_list = new LinkedList<String>();

        //Setting Stats

        Status base_status = new Status();
        base_status.HP = job.getHPForLevel(level);
        base_status.STR = job.getSTRForLevel(level);
        base_status.DEX = job.getDEXForLevel(level);
        base_status.INT = job.getINTForLevel(level);
        base_status.ABILITY_COUNT = job.ability_count;

        // hide shadow
        hide();


        EquipmentForLevel equipments_set = job.getEquipmentForLevel(level);
        String[] equipment_names = null;
        if (equipments_set != null) equipment_names = equipments_set.chooseEquipmentSet();
        if (equipment_names != null) {
            inventory = new PersonalInventory(Assets.getSkin(), equipment_names, null/* job.item_name*/, this, null);
        }
        status = new TotalStatus(this, base_status, inventory);
        this.addActor(turn_effect_info);
        //this.addActor(target_info);
        level_up_window = new com.icefill.game.actors.windows.LevelUpWindow(this, Assets.getSkin());

        //this.addActor(info_table);
    }

    public void setJob(Job job_to_set, boolean initial_job) {
        this.job = job_to_set;
        this.type = job_to_set.type;
        this.sprites = job_to_set.default_sprites;
        if (job_to_set.move_ability != null) {
            move_ability = new ActionContainer(Assets.actions_map.get(job_to_set.move_ability), 0);
        }
        if (job_to_set.dead_ability != null) {
            dead_ability = ObjActions.getSubAction(job_to_set.dead_ability);
        }

        //Setting attainable abilities
        if (job_to_set.attainable_ability != null) {
            attain_ability_list = new LinkedList<AbilityActor>();
            for (String ability_name : job_to_set.attainable_ability) {
                attain_ability_list.add((AbilityActor) (Assets.actions_map.get(ability_name)));
            }
            Collections.sort(attain_ability_list, new RequiredLevelCompare());
        }
        if (job_to_set.attainable_passive_ability != null) {
            attain_passive_ability_list = new LinkedList<AbilityActor>();
            for (String ability_name : job_to_set.attainable_passive_ability) {
                attain_passive_ability_list.add((AbilityActor) (Assets.actions_map.get(ability_name)));
            }
            Collections.sort(attain_passive_ability_list, new RequiredLevelCompare());
        }
        // for light
        if (job_to_set.glow != null) {
            is_glowing = true;
            glow = (NonObjSprites) Assets.non_obj_sprites_map.get("glow");
        }


        if (initial_job) {
            //Setting ability
            if (job_to_set.ability_name != null) {
                ability_list = new LinkedList<ActionContainer>();
                for (String ability_name : job_to_set.ability_name) {
                    String name;
                    int level;
                    if (ability_name.startsWith("L#")) {
                        name=ability_name.substring(4);
                        level=Integer.parseInt(ability_name.substring(2,3));
                    }
                    else
                    {
                        name=ability_name;
                        level=0;
                    }
                    ability_list.add(new ActionContainer(Assets.actions_map.get(name), level));
                }
            }
        } else {
            if (job_to_set.ability_name != null) {
                for (String ability_name : job_to_set.ability_name) {
                    String name;
                    int level;
                    if (ability_name.startsWith("L#")) {
                        name=ability_name.substring(4);
                        level=Integer.parseInt(ability_name.substring(2,3));
                    }
                    else
                    {
                        name=ability_name;
                        level=0;
                    }
                    Boolean contains = false;
                    for (ActionContainer temp_cont : ability_list) {
                        if (temp_cont.action.getActionName().equals(name)) {
                            temp_cont.level= (temp_cont.level<level)?level:temp_cont.level;
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        ability_list.add(new ActionContainer(Assets.actions_map.get(name), level));
                    }
                }
            }
            EquipmentForLevel equipments_set = job_to_set.getEquipmentForLevel(level);
            String head_name = null;
            if (equipments_set != null) head_name = equipments_set.chooseEquipmentSet()[0];
            if (head_name != null) {
                EquipActor head_item = new EquipActor(head_name);
                this.inventory.setEquip(0, head_item);

            }

        }
        this.fire_level = job_to_set.fire_level;
        this.lightning_level = job_to_set.lightning_level;
        this.holy_level = job_to_set.holy_level;
        this.unholy_level = job_to_set.unholy_level;


    }

    public boolean inflictDamage(int amount, ObjActor attacker) {

        this.status.inflictDamage(amount);
        this.addAction(Actions.parallel(
                this.basicHitAction(Global.dungeon, attacker)
                , Actions.run(new Runnable() {
                    public void run() {
                        ObjActor.this.checkDeadandExecuteDead(Global.dungeon);
                    }
                })
        ));
        checkAndInitiateFleeing();
        // Check RunawayGAge

        return true;

    }

    public boolean checkAndInitiateFleeing() {
        if (this.job.runaway_ratio >= this.status.getCurrentHPRatio()) {
            //if (this.runaway_counter<=0)
            //{
            runaway_counter = 3;
            return true;
            //}
        }
        return false;
    }

    public void setSummonedObj() {
        is_summoned = true;
    }

    public boolean isSummoned() {
        return is_summoned;
    }

    public boolean isFleeing() {
        if (runaway_counter > 0) return true;
        else return false;
    }

    public void decreaseRunawayCount() {
        if (runaway_counter > 0)
            runaway_counter--;
    }

    public void initializeRunawayCount() {
        runaway_counter = 0;
    }

    public boolean inflictDamageInRatio(float ratio, ObjActor attacker) {
        float amount = status.total_status.HP * ratio;
        inflictDamage((int) amount, attacker);
        return true;

    }

    public String getType() {
        return type;
    }

    public int getControlled() {
        return controlled;
    }

    public boolean isLearnable(AbilityActor action) {
        int current_level = 0;
        for (ActionContainer temp : ability_list) {
            if (temp.action.equals(action)) {
                current_level = temp.level;
                break;
            }
        }

        if (action.required_level + current_level <= this.level) {

            if (action.action_type.equals("fire_magic")) {

                if (action.action_level <= this.fire_level)
                    return true;
            } else if (action.action_type.equals("lightning_magic")) {
                if (action.action_level <= this.lightning_level)
                    return true;
            } else if (action.action_type.equals("holy_magic")) {
                if (action.action_level <= this.holy_level)
                    return true;
            } else if (action.action_type.equals("unholy_magic")) {
                if (action.action_level <= this.unholy_level)
                    return true;
            }
            Global.showBigMessage("Not Enough Magic Level (" + action.action_type + " " + action.action_level + ")");
        } else {
            Global.showBigMessage("Not Enough Level (Required " + action.required_level + " but " + current_level + ")");
        }
        return false;
		/*
		if (learnable_magic_type_list!=null &&learnable_magic_type_list.contains(type))
			return true;
		else 
			return false;
			*/
    }

    public boolean isDead() {
        if (obj_state == PL_DEAD)
            return true;
        else
            return false;
    }


    //setter methods

    //animation methods
    public void setAnimation(int ani) {
        if (((ObjSprites) sprites).hasAnimation(ani)) {
            curr_ani = ani;
        }
    }

    public float getAnimationDuration(int animation, int direction) {
        return sprites.getSpritesDuration(animation, direction);
    }

    public void setIdleAnimation(int ani) {
        idle_animation = ani;
    }

    public int getIdleAnimation() {
        return idle_animation;
    }

    public void setCurrentAnimationToIdleAnimation() {
        curr_ani = idle_animation;
    }

    public void setObsDestroyflag() {
        obs_destroy_flag = true;
    }

    public void releaseObsDestroyflag() {
        obs_destroy_flag = false;
    }

    public boolean ObsDestroy() {
        return obs_destroy_flag;
    }

    public void setPositionChanged() {
        is_position_changed = true;
    }

    public boolean isPositionchanged() {
        if (is_position_changed) {
            is_position_changed = false;
            return true;
        } else return false;
    }

    public int getTeam() {
        return team;
    }

    ;

    public ActionContainer getSelectedAction() {
        return selected_action;
    }

    public int getCurrentAnimation() {
        return curr_ani;
    }

    public Job getJob() {
        return job;
    }

    public void draw(Batch batch, float delta) {

        if (obj_state != PL_DEAD) {
            batch.setColor(1f, 1f, 1f, 0.6f);
            batch.draw(((ObjSprites) sprites).getShadow(), getX() - 16, getY() - 8);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        applyTransform(batch, computeTransform());
        ((ObjSprites) sprites).drawAnimation(batch, elapsed_time, curr_ani, curr_dir, 0, getZ(), 0, 0, 0, 1, 1, inventory, job.color);
        if (obj_state != PL_DEAD) {
            char_ui.draw_health(elapsed_time, batch, status.current_hp, status.total_status.HP, status.current_ap, this.selected);
            resetTransform(batch);

            if (is_glowing) {
                batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_SRC_ALPHA);
                batch.setColor(1f, 1f, 1f, 1f);

                glow.drawAnimation(batch, elapsed_time, 1, DIR.DL, getX() + job.glow[0], getY() + job.glow[1]
                        , 0, job.glow[2], job.glow[2]);
                batch.setColor(1f, 1f, 1f, 1f);

                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        } else resetTransform(batch);
        super.draw(batch, delta);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void draw(Batch batch, float delta, float x, float y) {
        ((ObjSprites) sprites).drawAnimation(batch, elapsed_time, curr_ani, curr_dir, x, y, 0, 0, 0, 1, 1, inventory, job.color);
    }

    public void act(float delta) {
        super.act(delta);
        turn_effect_info.setPosition(-10, 0);//+getZ());
        if (selected && !action_selected) {
            checkActionSelected();
        }
        if (obj_state != PL_DEAD && !hide_shadow)
            //tile_body.setTransform(getX(), getY()+getZ(),45);
            if (inventory != null) {
                inventory.act(delta);
            }
    }

    private void checkActionSelected() {
        if (ability_list != null)

            for (ActionContainer temp : ability_list) {
                if (temp.action.isSelected()) {

                    selected_action = temp;
                    action_selected = true;
                    return;
                }
            }
        if (getInventory() != null) {
            for (int i = 1; i < 4; i++) {
                EquipActor temp_equip = getInventory().getEquippingSlot(i).getEquip();
                if (temp_equip != null && temp_equip.equip_action != null) {
                    ActionContainer temp = temp_equip.equip_action;
                    if (temp.action.isSelected()) {
                        selected_action = temp;
                        action_selected = true;
                        return;
                    }

                }
            }
        }

    }

    public boolean isActionSelected() {
        if (action_selected) {
            action_selected = false;
            selected_action.action.selected = false;
            return true;
        } else return false;
    }

    public int getSelectedEquipIndex() {
        return selected_equip;
    }

    public void setSelectedEquipIndex(int i) {
        selected_equip = i;
    }

    public void selectAction(ActionContainer temp) {
        selected_action = temp;
    }

    public void selectActor() {
        selected = true;
    }

    public void deSelectActor() {
        selected = false;
        end();
        action_selected = false;
        selected_action = null;
    }

    public float lineDistance(ObjActor b) {
        return (float) Math.sqrt((double) ((this.getXX() - b.getXX()) * (this.getXX() - b.getXX()) + (this.getYY() - b.getYY()) * (this.getYY() - b.getYY())));
    }

    public float lineDistance(com.icefill.game.actors.dungeon.AreaCell b) {
        return (float) Math.sqrt((double) ((this.getXX() - b.getXX()) * (this.getXX() - b.getXX()) + (this.getYY() - b.getYY()) * (this.getYY() - b.getYY())));
    }


    public LinkedList<ActionContainer> getActionList() {
        return ability_list;

    }

    public ActionContainer getMoveAction() {
        return move_ability;
    }

    public void addAbility(AbilityActor ability) {
        if (ability.action_type.equals("passive")) {
            attain_passive_ability_list.remove(ability);
            ability.executePassive(Global.dungeon, this, 0);
            passive_action_list.add(ability);
        } else {
            for (ActionContainer temp_container : ability_list) {
                if (temp_container.action.equals(ability)) {
                    temp_container.level++;
                    return;
                }
            }
            attain_ability_list.remove(ability);
            showAbilityIcon(ability);
            ability_list.add(new ActionContainer(ability, 0));
        }


    }

    public void showAbilityIcon(ActionActor ability) {
        if (ability != null) {
            final Actor icon = ability.getIconActor();
            if (icon != null) {
                ability_info.addTargetInfo("new skill\n\n\n");
                ability_info.addActor(icon);
                icon.setPosition(20, 10);
                ability_info.addAction(
                        Actions.sequence(
                                //Actions.moveTo(200,100,.3f),
                                Actions.fadeIn(.2f),
                                Actions.delay(3.5f),
                                //Actions.fadeOut(.2f),
                                Actions.run(new Runnable() {
                                    public void run() {
                                        icon.remove();
                                        ability_info.removeTargetInfo();
                                    }
                                })
                                //Actions.removeActor()
                        )

                );
            }
        }
    }

    public ActionActor getRandomAbilityToLearn() {
        ActionActor ability = null;
        ActionActor passive_ability = null;
        if (!attain_ability_list.isEmpty()) {
            ability =
                    attain_ability_list.get(Randomizer.nextInt(attain_ability_list.size()));
        }
        if (attain_passive_ability_list != null && !attain_passive_ability_list.isEmpty()) {
            passive_ability =
                    attain_passive_ability_list.get(Randomizer.nextInt(attain_passive_ability_list.size()));
        }
        if (passive_ability != null && Randomizer.nextFloat() > .5f) {
            return passive_ability;
        }
        return ability;

    }


    public LinkedList<StatusTuple.TURNEFFECT> getTurnEffectLIst() {
        return turn_effect_list;
    }

    public void addTurnEffect(StatusTuple.TURNEFFECT turn_effect) {
        Iterator<StatusTuple.TURNEFFECT> iter = turn_effect_list.iterator();
        while (iter.hasNext()) {
            StatusTuple.TURNEFFECT temp_effect = iter.next();
            if (temp_effect.turn_effect_name != null && temp_effect.turn_effect_name.equals(turn_effect.turn_effect_name)) {
                iter.remove();
            }
        }
        turn_effect_list.add(turn_effect);
        status.setStatus(getInventory(), getTurnEffectLIst());
        addTurnEffectInfo(turn_effect.turn_effect_name + (turn_effect.duration - turn_effect.current_turn));

        if (turn_effect.restricted_ability_type != null) {
            setForbiddenActionTypeList();
        }
    }

    public void setForbiddenActionTypeList() {
        forbidden_action_type_list.clear();
        for (StatusTuple.TURNEFFECT turn_effect : turn_effect_list) {
            if (turn_effect.restricted_ability_type != null)
                for (String action_type : turn_effect.restricted_ability_type) {
                    if (!forbidden_action_type_list.contains(action_type)) {
                        forbidden_action_type_list.add(action_type);
                    }
                }
        }
        if (ability_list != null)
            for (ActionContainer action_container : ability_list) {
                if (forbidden_action_type_list.contains(action_container.action.getActionType())) {
                    action_container.is_forbidden = true;
                } else {
                    action_container.is_forbidden = false;
                }
            }
        if (forbidden_action_type_list.contains("move")) {
            this.move_ability.is_forbidden = true;
        } else if (this.move_ability != null) {
            this.move_ability.is_forbidden = false;
        }

    }

    public boolean isAvailableAction(ActionContainer temp) {
        if (temp == null) return false;
        else if (!(temp.action instanceof AbilityActor)
                ||
                (
                        ((AbilityActor) temp.action).mana_cost <= Global.dungeon.getTeam(this.team).getMana() &&
                                (temp.current_cool_time <= 0) &&
                                !(temp.is_forbidden) &&
                                ((AbilityActor) temp.action).checkWeaponType(this)
                )
                ) {
            return true;
        } else return false;
    }

    public void clearTurnEffect() {

        removeTurnEffectInfo();

        for (StatusTuple.TURNEFFECT temp_effect : turn_effect_list) {
            if (temp_effect.effect != null) {
                temp_effect.effect.end();
            }

            if (temp_effect.end_motions != null && !temp_effect.turn_effect_name.equals("revive")) {
                for (com.icefill.game.actors.actionActors.Function temp_function : temp_effect.end_motions) {
                    temp_function.execute(Global.dungeon, this, null, null);

                }
            }

        }
        turn_effect_list.clear();
        setForbiddenActionTypeList();
        this.status.setStatus(inventory, turn_effect_list);
    }

    public void doTurnEffect(com.icefill.game.actors.dungeon.DungeonGroup room) {
        for (Iterator<StatusTuple.TURNEFFECT> itr = turn_effect_list.iterator(); itr.hasNext(); ) {
            StatusTuple.TURNEFFECT temp_effect = itr.next();
            if (temp_effect.amount != 0) {
                SequenceAction seq = new SequenceAction();

                this.addAction(seq);
                this.showStatusChange("DAMAGED:" + temp_effect.amount, 0);
                this.inflictDamage(temp_effect.amount, null);

            }
            temp_effect.current_turn++;
            addTurnEffectInfo(temp_effect.turn_effect_name + (temp_effect.duration - temp_effect.current_turn));
            if (temp_effect.current_turn >= temp_effect.duration) {
                if (temp_effect.effect != null) {
                    temp_effect.effect.end();
                }
                if (temp_effect.release_message == null)
                    this.showStatusChange("effect over", 0);
                else
                    this.showStatusChange(temp_effect.release_message, 0);

                this.removeTurnEffectInfo();
                if (temp_effect.end_motions != null) {
                    for (Function temp_function : temp_effect.end_motions) {
                        temp_function.execute(room, this, null, null);
                    }
                }
                itr.remove();
                if (temp_effect.restricted_ability_type != null) {
                    setForbiddenActionTypeList();
                }
                this.status.setStatus(inventory, turn_effect_list);

            }
        }

    }


    public ObjInfoWindow getSkillWindow() {
        return this.skill_window;
    }

    public LevelUpWindow getLevelUpWindow() {
        return this.level_up_window;
    }

    //*********************************************** Actions ******************************************/
    @Override
    public void setDirection(DIR dir) {
        super.setDirection(dir);
        if (inventory != null) {
            if (inventory.getEquip(2) != null && inventory.getEquip(2).getType() == 2)
                inventory.getEquip(2).setDirection(dir);
            if (inventory.getEquip(3) != null && inventory.getEquip(3).getType() == 2)
                inventory.getEquip(3).setDirection(dir);
        }

        //if (curr_dir!=dir)curr_dir=dir;
    }

    public DIR getDirectionToTarget(com.icefill.game.actors.dungeon.AreaCell cell) {
        if (cell != null)
            return getDirectionToTarget(cell.getXX(), cell.getYY());
        else
            return curr_dir;
    }

    public void setDirectionToTarget(ObjActor obj) {
        com.icefill.game.actors.dungeon.AreaCell cell = Global.getCurrentRoom().getCell(obj);
        if (cell != null) {
            setDirectionToTarget(cell);
            return;
        } else
            return;
    }

    public void setDirectionToTarget(com.icefill.game.actors.dungeon.AreaCell cell) {
        if (this.getXX() == cell.getXX() && this.getYY() == cell.getYY()) {
            return;
        } else {
            setDirection(getDirectionToTarget(cell.getXX(), cell.getYY()));
        }
    }

    public Action setDirectionToTargetAction(final ObjActor obj) {
        return Actions.run(new Runnable() {
            public void run() {
                setDirectionToTarget(obj);
            }
        });
    }

    public Action setDirectionToTargetAction(final com.icefill.game.actors.dungeon.AreaCell cell) {
        return Actions.run(new Runnable() {
            public void run() {
                setDirectionToTarget(cell);
            }
        });
    }

    public Action ConcileShadowAction() {
        return Actions.run(new Runnable() {
            public void run() {
                concileShadow();
            }
        });
    }

    public Action RevealShadowAction() {
        return Actions.run(new Runnable() {
            public void run() {
                revealShadow();
            }
        });
    }

    public Action raisedAction() {
        SequenceAction seq = new SequenceAction();
        seq.addAction(Actions.moveBy(0, 8, 0.08f));
        seq.addAction(Actions.moveBy(0, -8, 0.08f));
        return seq;
    }

    public Action pulledAction(DIR direction) {
        return pulledAction(direction, 1f);
    }

    public Action pulledAction(DIR direction, float multiplier) {
        SequenceAction seq = new SequenceAction();
        Vector2 vector = direction.toScreenVector().scl(10 * multiplier);
        seq.addAction(Actions.moveBy(vector.x, vector.y, 0.05f));
        seq.addAction(Actions.moveBy(-vector.x, -vector.y, 0.25f));
        return seq;
    }

    public Action GuardAction(ObjActor attacker, com.icefill.game.actors.dungeon.DungeonGroup dungeon) {

        Sound hit_sound = Assets.getAsset("sound/guard.wav", Sound.class);
        hit_sound.play(50f);

        SequenceAction seq = new SequenceAction();
        DIR direction_guard = attacker.getDirectionToTarget(getXX(), getYY());

        seq.addAction(setDirectionAction(direction_guard.opposite()));

        Vector2 vector_direction = direction_guard.toScreenVector().scl(6);

        if (!dodging) {
            backup_pos_x = getX();
            backup_pos_y = getY();
        }

        seq.addAction(Actions.parallel(
                this.animationAction(GUARD, 1, .5f)
                , Actions.sequence(
                        this.dodgeOn(),
                        Actions.moveTo(backup_pos_x + vector_direction.x, backup_pos_y + vector_direction.y, 0.04f),
                        Actions.delay(.2f),
                        Actions.moveTo(backup_pos_x, backup_pos_y, 0.2f),
                        this.dodgeOff()

                )
                )
        );
        return seq;
    }

    public Action DodgeAction(DIR direction, com.icefill.game.actors.dungeon.DungeonGroup dungeon) {
        SequenceAction seq = new SequenceAction();
        int x = 0;
        int y = 0;
        direction = direction.turnRight(1);
        Vector2 vector_direction = direction.toScreenVector().scl(15);
        if (!dodging) {
            backup_pos_x = getX();
            backup_pos_y = getY();
        }
        dodge_direction *= -1;
        seq.addAction(this.startAction());
        seq.addAction(this.deActivateAction());
        seq.addAction(this.dodgeOn());
        seq.addAction(Actions.parallel(
                this.pauseAnimation(.4f),
                Actions.sequence(
                        Actions.moveTo(backup_pos_x + vector_direction.x * dodge_direction, backup_pos_y + vector_direction.y * dodge_direction, 0.10f),
                        Actions.delay(.2f),
                        ExtendedActions.moveToParabolic(backup_pos_x, backup_pos_y, getZ(), .16f),
                        this.dodgeOff(),
                        this.endActionSubAction()
                )
                )
        );

        return seq;
    }

    public Action pushAction(DIR direction) {
        SequenceAction seq = new SequenceAction();
        int x = 0;
        int y = 0;
        switch (direction) {
            case DL:
                x = -4;
                y = -2;
                break;
            case DR:
                x = 4;
                y = -2;
                break;
            case UR:
                x = 4;
                y = 2;
                break;
            case UL:
                x = -4;
                y = 2;
                break;
        }
        seq.addAction(Actions.moveBy(-x, -y, 0.08f));
        seq.addAction(Actions.moveBy(x, y, 0.08f));
        return seq;
    }


    public Action changeAnimationSubAction(final int animation) {
        return Actions.run(new Runnable() {
            public void run() {
                elapsed_time = 0;
                if (((ObjActor) self).getCurrentAnimation() != animation) ((ObjActor) self).setAnimation(animation);
            }
        });
    }


    public Action changeIdleAnimationSubAction(final int animation) {
        return Actions.run(new Runnable() {
            public void run() {
                ((ObjActor) self).setIdleAnimation(animation);
            }
        });
    }

    public void gainExperience(int amount, com.icefill.game.actors.dungeon.DungeonGroup dungeon) {
        showStatusChange("EXP:" + amount, 0);
        experience += amount;
        this.checkLevelUp(dungeon);

    }

    public void gainExperience(float multiplier, ObjActor opponent, com.icefill.game.actors.dungeon.DungeonGroup dungeon, boolean check_level_up) {
        float d_level;
        if (opponent != null) {
            d_level = this.level - opponent.level;
            if (d_level < 0) d_level = 1 / (-d_level);
            else d_level++;
        } else {
            d_level = 1;
        }
        if (check_level_up)
            gainExperience((int) (multiplier * d_level), dungeon);
        else
            gainTempExperience((int) (multiplier * d_level), dungeon);
    }


    public void gainTempExperience(int amount, com.icefill.game.actors.dungeon.DungeonGroup dungeon) {
        showStatusChange("EXP:" + amount, 0);
        temp_exp += amount;
    }

    public int computeExpToLevelUp(int current_level) {
        return (int) (100 * Math.pow(2, current_level));
    }

    public void checkLevelUp(final com.icefill.game.actors.dungeon.DungeonGroup dungeon) {
        Global.gfs.pauseGFS();
        experience += temp_exp;
        temp_exp = 0;
        while (computeExpToLevelUp(this.level) <= experience) {
            level++;
            this.addAction(Actions.sequence(
                    Actions.delay(.8f),
                    Actions.run(new Runnable() {
                                    public void run() {
                                        Sound hit_sound = Assets.getAsset("sound/levelup.wav", Sound.class);
                                        hit_sound.play(1f, 1f, 0f);
                                        showStatusChange("LEVEL UP", 0);
                                        level_up_window.levelUp(dungeon);

                                    }
                                }
                    )
            ));
        }
        Global.gfs.reRunGFS();
    }

    public Action basicHitAction(final com.icefill.game.actors.dungeon.DungeonGroup room, ObjActor to_act) {
        DIR direction;
        if (to_act != null) {
            direction = to_act.getDirectionToTarget(this.getXX(), this.getYY());
        } else direction = DIR.DL;
        int multiplier = 1;
        Sound hit_sound = Assets.getAsset("sound/hit.wav", Sound.class);
        hit_sound.play();

        if (direction .equals( DIR.DR )|| direction.equals( DIR.UR)) multiplier = -1;
        SequenceAction seq = new SequenceAction();
        seq.addAction(//Actions.after(
                Actions.sequence(
                        Actions.parallel(
                                room.getGFSM().pauseGFSAction()
                                , Actions.delay(.5f)
                                , this.pulledAction(direction, .5f)
                                , Actions.rotateBy(2 * multiplier, 0.1f)
                                , Actions.rotateBy(-2 * multiplier, 0.4f)
                                , room.getGFSM().reRunGFSAction()
                        )
                )
        );
        return seq;

    }

    public Action basicShakeAction() {
        SequenceAction seq = new SequenceAction();
        seq.addAction(Actions.moveBy(3f, 0, .04f));
        seq.addAction(Actions.moveBy(-3f, 0, .07f));
        return seq;
    }

    public boolean checkDeadandExecuteDead(final com.icefill.game.actors.dungeon.DungeonGroup room) {
        if (status.current_hp <= 0 && obj_state != PL_DEAD) {
            status.current_hp = 0;
            obj_state = PL_DEAD;
            if (!this.isObstacle()) {
                addAction(Actions.sequence(
                        room.getGFSM().pauseGFSAction(),
                        Actions.delay(.4f),
                        Actions.run(new Runnable() {
                            public void run() {
                                ObjActor.this.showMessage("I'm Dying...");
                            }
                        }),
                        Actions.delay(.5f),
                        (deadAction(room)),
                        Actions.delay(.3f),
                        Actions.run(new Runnable() {
                            public void run() {
                                Global.showMessage(ObjActor.this.job.job_name + " is dead.", 0);
                                if (room.getAttacker() != null) {
                                    room.getAttacker().gainExperience(15.0f, ObjActor.this, room, false);
                                }
                            }
                        }),

                        room.getGFSM().reRunGFSAction(),

                        Actions.run(new Runnable() {
                            public void run() {
                                setFrontBack(-1);
                            }
                        })

                        )
                );
            } else {
                addAction(Actions.sequence(
                        room.getGFSM().pauseGFSAction(),
                        Actions.after(deadAction(room)),
                        Actions.delay(.3f),
                        room.getGFSM().reRunGFSAction()
                ));
            }

            return true;
        }
        return false;

    }

    public Action deadAction(final com.icefill.game.actors.dungeon.DungeonGroup room) {
        SequenceAction to_return = new SequenceAction();
        if (this.obj_state != DEAD) {
            clearTurnEffect();
            this.setColor(Color.WHITE);

            room.getCurrentRoom().removeActor(this.getXX(), this.getYY());
            //this.obj_state=PL_DEAD;

            if (room.getAttacker() != null && !this.isObstacle()) {
                if (room.getAttacker().level < this.level)
                    room.getAttacker().gainTempExperience(10 + 3 * (this.level - room.getAttacker().level), room);
                else
                    room.getAttacker().gainTempExperience(10, room);
            }

            com.icefill.game.actors.dungeon.AreaCell tempCell = room.current_map.getCell(this.getXX(), this.getYY());

            if (this.team != 0 && room.current_map.getCell(this.getXX(), this.getYY()) != null && tempCell.device == null && this.dead_ability == null) {
                if (Randomizer.hitInRatio(.6f))
                if (Randomizer.hitInRatio(0.2f)) {
                    if (this.inventory != null && !this.isLeader()) {
                        EquipActor relic = this.inventory.getRandomEquip();
                        if (relic != null && tempCell.device == null) {
                            tempCell.device = new ItemActor(relic, tempCell, Global.getCurrentRoom());
                        }
                    }
                }
                else {
                    if (Randomizer.hitInRatio(0.65f))
                        tempCell.device = new GoldActor(15, tempCell, Global.getCurrentRoom());
                    else {
                        tempCell.device= new ItemActor(new EquipActor(Global.dungeon.item_pool.getItem(Global.dungeon.room_zzz+1)),tempCell,Global.dungeon.getCurrentRoom());
                    }
                }
            }

            to_return.addAction(changeAnimationSubAction(DEAD));

            if (dead_ability != null) {
                to_return.addAction(Actions.run(new Runnable() {
                    public void run() {

                        dead_ability.execute(room, ObjActor.this, null, null);
                    }
                }));

            }
            this.status.current_ap = 0;

            to_return.addAction(Actions.run(new Runnable() {
                public void run() {
                    if (ObjActor.this.team == 0)
                        room.team_lists[ObjActor.this.team].remove(self);
                    Global.gfs.checkBattleOverandChangeState();
                }
            }));
        }

        return to_return;
    }

    public void reviveAction(DungeonGroup room) {

        this.obj_state = PL_DONE;
        setFrontBack(0);
        this.remove();
        Global.getCurrentRoom().addActor(this);
        this.addAction(Actions.sequence(animationAction(RAISE, 1, 0f), changeAnimationSubAction(IDLE)));

    }

    public Action animationAction(final int animation, int n, float pause_time) {
        SequenceAction seq = new SequenceAction();
        seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        ((ObjActor) self).setAnimation(animation);
                        ;
                        ObjActor.this.elapsed_time = 0f;
                    }
                })
        );
        seq.addAction(Actions.delay((float) (((ObjActor) self).getAnimationDuration(animation, DIR.DL.v))));
        seq.addAction(((ObjActor) self).pauseAnimation(pause_time));
        seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        if (ObjActor.this.obj_state != PL_DEAD) {
                            ((ObjActor) self).setAnimation(getIdleAnimation());
                            ;
                            ObjActor.this.elapsed_time = 0f;
                        }
                    }
                })
        );

        return seq;

    }

    public Action reverseAnimationAction(final int animation, int n, float pause_time) {
        SequenceAction seq = new SequenceAction();
        seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        ((ObjSprites) sprites).setAnimationReverse(animation);
                        ((ObjActor) self).setAnimation(animation);
                        ;
                        ObjActor.this.elapsed_time = 0f;
                    }
                })
        );
        seq.addAction(Actions.delay((float) (((ObjActor) self).getAnimationDuration(animation, DIR.DL.v))));
        seq.addAction(((ObjActor) self).pauseAnimation(pause_time));
        seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        ((ObjSprites) sprites).setAnimationNormal(animation);
                        if (ObjActor.this.obj_state != PL_DEAD) {
                            ((ObjActor) self).setAnimation(getIdleAnimation());
                            ;
                            ObjActor.this.elapsed_time = 0f;
                        }
                    }
                })
        );

        return seq;

    }

    public Action setAnimationAction(final int animation) {
        return Actions.run(new Runnable() {
            @Override
            public void run() {
                ((ObjActor) self).setAnimation(animation);
                ;
                ObjActor.this.elapsed_time = 0f;
            }
        });
    }

    public Action changeMapPositionSubAction(final RoomGroup obj_group, final int xx, final int yy) {
        return Actions.run(new Runnable() {
            public void run() {
                obj_group.changeActorPosition(ObjActor.this, xx, yy);
            }
        });
    }

    public Action dodgeOn() {
        return Actions.run(new Runnable() {
            public void run() {
                dodging = true;
            }
        });
    }

    public Action dodgeOff() {
        return Actions.run(new Runnable() {
            public void run() {
                dodging = false;
            }
        });
    }

    public Action rotateEquipSubAction(final float from, final float to, final float duration) {
        return Actions.run(new Runnable() {
            public void run() {
                //inventory.getEquip(2).setRotation(from);
                Action action = Actions.rotateTo(270, duration);
                action.setTarget(inventory.getEquip(2));
                inventory.getEquip(2).addAction(action);
            }
        });
    }

    public void showStatusChange(String message, int type) {

        if (!(getType().equals("wall") && !getType().equals("obstacle"))) {
            status_change_label_index++;
            Label.LabelStyle style = new Label.LabelStyle();
            style.font = Assets.getFont();
            final Label status_change_label = new Label(message, style);
            status_change_label.setPosition(-20, 40);
            status_change_label.setVisible(false);
            status_change_label.setVisible(true);
            status_change_label.pack();
            this.addActor(status_change_label);
            status_change_label.addAction(
                    Actions.parallel(
                            Actions.sequence(
                                    Actions.delay(2f),
                                    //Actions.fadeOut(1.5f),
                                    Actions.run(new Runnable() {
                                        public void run() {
                                            status_change_label.setVisible(false);
                                        }
                                    }),
                                    Actions.fadeIn(0)

                            )
                            ,
                            Actions.sequence(
                                    //Actions.delay(.8f),
                                    Actions.delay(.5f),
                                    Actions.moveBy(0, 6 + 8 * status_change_label_index, .5f),
                                    Actions.delay(.7f),
                                    Actions.run(new Runnable() {
                                        public void run() {
                                            status_change_label_index--;
                                        }
                                    }),
                                    Actions.removeActor()
                            )
                    )

            );
        }

    }

    public void addTargetInfo(String message) {
        if (!getType().equals("wall") && !getType().equals("obstacle")) {

            target_info.addTargetInfo(message);
            Global.getCurrentRoom().addActor(target_info);
        }
    }

    public void showMessage(String message) {
        showMessage(message, 0.3f);
    }

    public void showMessage(String message, float time) {
        if (!getType().equals("wall") && !getType().equals("obstacle")) {

            target_info.addTargetInfo(message);
            target_info.addAction(
                    Actions.sequence(
                            Actions.fadeIn(time),
                            Actions.delay(time + 0.2f),
                            Actions.run(new Runnable() {
                                public void run() {
                                    target_info.removeTargetInfo();
                                }
                            })
                    )

            );

        }
    }

    public void addTurnEffectInfo(String message) {
        if (!this.isWall() && !this.isObstacle()) {
            turn_effect_info.setText(message);
            turn_effect_info.setVisible(true);
            turn_effect_info.pack();
        }
    }

    public void removeTargetInfo() {
        target_info.removeTargetInfo();
    }

    public void removeTurnEffectInfo() {
        turn_effect_info.setVisible(false);
    }

    public void addCoolTimes() {
        for (ActionContainer temp_action : ability_list) {
            temp_action.addCoolTime();
        }
    }

    public void subCoolTimes() {
        for (ActionContainer temp_action : ability_list) {
            temp_action.subCoolTime();
        }
        if (getInventory() != null) {
            for (int i = 1; i < 4; i++) {
                EquipActor temp_equip = getInventory().getEquippingSlot(i).getEquip();
                if (temp_equip != null && temp_equip.equip_action != null) {
                    ActionContainer temp = temp_equip.equip_action;
                    temp.subCoolTime();
                }
            }
        }
        if (move_ability != null) {
            move_ability.subCoolTime();
        }
    }

    public void resetCoolTimes() {
        for (ActionContainer temp_action : ability_list) {
            temp_action.resetCoolTime();
        }
        if (move_ability != null) {
            move_ability.resetCoolTime();
        }
    }

    public String toString() {
        String to_return = "";
        to_return +=
                "Job: " + this.job.job_name + "\n" +
                        "Ctr: " + controlled_name[this.controlled] + "\n" +
                        (status != null ? status.toString() : "\n") +
                        "Lvl: " + this.level + "\n" +
                        "EXP: " + this.experience + "\n\n *MAGIC LEVEL:\n" +
                        " FIRE: " + this.job.fire_level + "\n LIGHTNING: " + this.job.lightning_level + "\n" +
                        " HOLY: " + this.job.holy_level + "\n UNHOLY   : " + this.job.unholy_level + "\n"
        ;

        return to_return;
    }

    public String getSelectedString() {
        String to_return = "";
        to_return +=
                this.job.job_name + "\n\n" +
                        "HP:" + this.status.current_hp + "\n" +
                        "LEVEL:" + this.level + "\n" +
                        "EXP:" + this.experience + "/(" + computeExpToLevelUp(this.level) + ")";
        return to_return;
    }


    public int getOneDistance(ObjActor temp) {
        return Math.abs(temp.getXX() - this.getXX())
                + Math.abs(temp.getYY() - this.getYY());
    }

    public int getOneDistance(AreaCell temp) {
        return Math.abs(temp.getXX() - this.getXX())
                + Math.abs(temp.getYY() - this.getYY());
    }

    public boolean isObstacle() {
        if (job != null && job.type.equals("obstacle")) return true;
        else return false;
    }

    public boolean isDangerous() {
        if (job != null && job.dangerous) return true;
        else return false;
    }

    public boolean isWall() {
        if (job != null && job.type.equals("wall")) return true;
        else return false;
    }

    public boolean isMeleeUnit() {
        if (job.ai_type.equals("melee")) return true;
        else return false;
    }

    public boolean isRangedUnit() {
        if (job.ai_type.equals("ranged")) return true;
        else return false;
    }

    public boolean isAssistUnit() {
        if (job.ai_type.equals("assist")) return true;
        else return false;
    }

    /***************************** For Read Json ******************************************/
    public static class Seed {
        String name;
        String[] equipment_name;
        String[] item_name;
        String type;
        String job;
        String sprites_name;
        boolean equippable;
        Status base_status;
        //int hp;
        int level;

        //int STR;
        //int DEX;
        //int INT;

        //int ability_count;
        String ability_name[];
        String move_ability;
    }

    public static class TotalStatus {
        public Status base_status;
        public Status equip_status;
        public Status turn_effect_status;
        public Status total_status;
        private int current_hp;
        private int current_ap;
        ObjActor obj;

        public TotalStatus(ObjActor obj, Status base_status, PersonalInventory inventory) {
            this.base_status = new Status(base_status);
            total_status = new Status();
            setStatus(inventory, null);
            current_hp = total_status.HP;
            current_ap = 0;
            this.obj = obj;
        }

        public TotalStatus() {
            base_status = new Status();
            total_status = new Status();
        }

        public void setStatus(PersonalInventory inventory, LinkedList<StatusTuple.TURNEFFECT> turn_effect_list) {

            total_status.STR = base_status.STR;
            total_status.DEX = base_status.DEX;
            total_status.INT = base_status.INT;
            total_status.ABILITY_COUNT = base_status.ABILITY_COUNT;
            total_status.HP = base_status.HP;
            total_status.DEFENSE = base_status.DEFENSE;
            total_status.DODGE = base_status.DODGE;

            if (inventory != null) {
                equip_status = inventory.getTotalEquipStatus();
                total_status.addStatus(equip_status);
            } else equip_status = new Status();
            if (turn_effect_list != null) {
                turn_effect_status = new Status();
                for (StatusTuple.TURNEFFECT temp_turn_effect : turn_effect_list) {
                    if (temp_turn_effect != null && temp_turn_effect.initial_status_change != null) {
                        turn_effect_status.addStatus(temp_turn_effect.initial_status_change);
                    }
                }
                total_status.addStatus(turn_effect_status);
            }
            total_status.DEFENSE = total_status.DEFENSE * (1 + (int) ((float) total_status.STR / 10f));
            total_status.DODGE = total_status.DODGE + total_status.DEX;//(int)(100*(float)total_status.DEX/50f);
            if (total_status.HP < current_hp)
                current_hp = total_status.HP;
            if (inventory != null) {
                inventory.renewStatus();
            }

        }

        public boolean inflictDamage(int amount) {
            if (amount > 0) {
                current_hp -= amount;
                if (current_hp < 0) current_hp = 0;
                obj.checkDeadandExecuteDead(Global.dungeon);

                return true;
            } else return false;
        }

        public boolean heal(int amount) {
            if (amount > 0) {
                current_hp += amount;
                if (current_hp > total_status.HP) current_hp = total_status.HP;
                return true;
            } else return false;
        }

        public int getCurrentHP() {
            return current_hp;
        }

        public void fullHP() {
            current_hp = total_status.HP;
        }

        public void setHPInRatio(float ratio) {
            current_hp = (int) (total_status.HP * ratio);
        }

        public boolean healInRatio(float ratio) {
            if (ratio > 0) {
                float amount = total_status.HP * ratio;
                heal((int) amount);
                return true;
            } else return false;
        }

        public float getCurrentHPRatio() {
            return ((float) current_hp) / total_status.HP;
        }

        public int getCurrentAP() {
            return current_ap;
        }

        public void setAP() {
            current_ap = total_status.ABILITY_COUNT;
        }

        public void setAP(int amount) {
            if (amount >= 0)
                current_ap = amount;
        }

        public void decreaseAP() {
            current_ap--;

        }

        public String toString() {
            return "STR:" + total_status.STR + "(" + equip_status.STR + ")" + "\n" +
                    "DEX:" + total_status.DEX + "(" + equip_status.DEX + ")" + "\n" +
                    "INT:" + total_status.INT + "(" + equip_status.INT + ")" + "\n" +
                    "AP:"  + total_status.ABILITY_COUNT + "(" + equip_status.ABILITY_COUNT + ")" + "\n" +
                    "HP:" + current_hp + "/" + total_status.HP + "(" + equip_status.HP + ")" + "\n" +
                    "DEF:" + total_status.DEFENSE + "(" + equip_status.DEFENSE + ")" + "\n" +
                    "DODGE:" + total_status.DODGE + "(" + equip_status.DODGE + ")" + "\n";

        }

    }


    public static class Status {
        public int STR;
        public int DEX;
        public int INT;
        public int ABILITY_COUNT;
        public int HP;

        public int DEFENSE;
        public int DODGE;

        public Status() {
        }

        public Status(Status status) {
            STR = status.STR;
            DEX = status.DEX;
            INT = status.INT;
            ABILITY_COUNT = status.ABILITY_COUNT;
            HP = status.HP;
            DEFENSE = status.DEFENSE;
            DODGE = status.DODGE;

        }

        public void addStatus(Status status) {
            if (status != null) {
                STR += status.STR;
                DEX += status.DEX;
                INT += status.INT;
                HP += status.HP;
                DEFENSE += status.DEFENSE;
                DODGE += status.DODGE;
                ABILITY_COUNT += status.ABILITY_COUNT;
            }
        }

        public void addStatus(EquipActor.EquipStatus equip_status) {
            if (equip_status != null) {
                STR += equip_status.STR_MODIFIER;
                DEX += equip_status.DEX_MODIFIER;
                INT += equip_status.INT_MODIFIER;
                HP += equip_status.HP_MODIFIER;
                DODGE += equip_status.DODGE_MODIFIER;
                DEFENSE += equip_status.DEFENSE;
            }
        }

    }

    static class RequiredLevelCompare implements Comparator<AbilityActor> {
        public int compare(AbilityActor arg0, AbilityActor arg1) {
            return arg0.required_level - arg1.required_level;
        }
    }

}

