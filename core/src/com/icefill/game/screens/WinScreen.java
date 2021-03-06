package com.icefill.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.icefill.game.Assets;
import com.icefill.game.Constants;
import com.icefill.game.SigmaFiniteDungeon;

public class WinScreen extends BasicScreen
  implements Constants
{
  SpriteBatch batch;
  Texture img;
  Texture img3;
  Image img2;
  Table table;

  public WinScreen(final SigmaFiniteDungeon game)
  {
    super(game);
    TextButton button = new TextButton("Accept", Assets.getSkin(), "default");
    button.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        game.setScreen(new MenuScreen(game));
      }
    });
    Gdx.input.setInputProcessor(this.ui_stage);
    this.img = new Texture("badlogic.jpg");

    this.table = new Table(Assets.getSkin());

    this.table.setFillParent(true);
    Label label = new Label("Congratulation. You beat the game!!!", new Label.LabelStyle(Assets.getSkin().getFont("default-font"), Color.WHITE) );
    label.setFontScale(3);
    //this.table.add("GAME OVER").center();
    table.add(label).center();
    this.table.row();

    Gdx.input.setInputProcessor(this.stage);

    this.table.add(button).size(120.0F, 50.0F).uniform().spaceBottom(10.0F);

    this.ui_stage.addActor(this.table);
  }
}