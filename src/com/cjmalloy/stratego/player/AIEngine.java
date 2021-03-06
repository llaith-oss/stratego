/*
    This file is part of Stratego.

    Stratego is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Stratego is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Stratego.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.cjmalloy.stratego.player;

import java.io.IOException;

import javax.swing.JOptionPane;

import com.cjmalloy.stratego.Board;
import com.cjmalloy.stratego.Engine;
import com.cjmalloy.stratego.Move;
import com.cjmalloy.stratego.Piece;
import com.cjmalloy.stratego.Settings;
import com.cjmalloy.stratego.Spot;
import com.cjmalloy.stratego.Status;




public class AIEngine extends Engine implements CompControls, UserControls
{
	private WView view = null;
	private AI ai = null;
	
	public AIEngine(WView v)
	{
		view = v;
		board = new Board();
		ai = new AI(board, this);
	}
	
	public void play()
	{
		if (status != Status.SETUP)
			return;
		
		if (board.getTraySize() == 0)
		{
			status = Status.PLAYING;
			if (turn!=Settings.bottomColor)
				requestCompMove();
		}
		else 
		{
			try {
				ai.getBoardSetup();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (Settings.bShowAll)
			board.showAll();
	}
	
	private void requestCompMove()
	{
		if (status == Status.PLAYING)
		if (turn != Settings.bottomColor)
			ai.getMove();
	}
	
	public void requestUserMove(Move m)
	{
		if (m == null) return;
		
		if (status == Status.SETUP)
		{
			if (setupRemovePiece(m.getFrom()))
					setupPlacePiece(m.getPiece(), m.getTo());
		}
		else
		{
			if (turn != Settings.bottomColor)
			{
				if (!AI.aiLock.isLocked())
					requestCompMove();
			}
			else
			{
				if (Settings.bShowAll)
					board.showAll();
				else if (!Settings.bNoHideAll)
					board.hideAll();
				
				if (requestMove(m))
					requestCompMove();
			}
		}
	}

	public void aiReturnMove(Move m)
	{
		if (turn == Settings.bottomColor)
			return;
		if (m.getPiece().getColor() == Settings.bottomColor)
			return; // shoulden't happen anyway
		
		if (m==null || m.getPiece()==null || m.getFrom()==null || m.getTo()==null)
		{
			//AI trapped
			status = Status.STOPPED;
			board.showAll();
			view.gameOver(Settings.bottomColor);
			return;
		}

		requestMove(m);
		if (turn != Settings.bottomColor)
			JOptionPane.showMessageDialog(null,
					"AI error" , "Critical Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void aiReturnPlace(Piece p, Spot s)
	{
		if (!setupPlacePiece(p, s))
			return;
		view.update();
	}

	@Override
	protected void gameOver(int winner)
	{
		view.gameOver(winner);
	}

	@Override
	protected void update()
	{
		view.update();
	}
}
