/*
 * Prison is a Minecraft plugin for the prison game mode.
 * Copyright (C) 2018 The Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.faizaan.prison.commands.handlers;

import xyz.faizaan.prison.Prison;
import xyz.faizaan.prison.commands.CommandArgument;
import xyz.faizaan.prison.commands.TransformError;
import xyz.faizaan.prison.internal.CommandSender;

public class DoubleArgumentHandler extends NumberArgumentHandler<Double> {

    public DoubleArgumentHandler() {
    }

    @Override public Double transform(CommandSender sender, CommandArgument argument, String value)
        throws TransformError {
        value = value.replace("$", "");
        value = value.replace("%", "");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new TransformError(
                Prison.get().getLocaleManager().getLocalizable("numberParseError")
                    .withReplacements(value).localizeFor(sender));
        }
    }
}