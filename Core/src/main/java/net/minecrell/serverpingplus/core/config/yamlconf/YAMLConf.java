/*
 *       _____                     _____ _         _____ _
 *      |   __|___ ___ _ _ ___ ___|  _  |_|___ ___|  _  | |_ _ ___
 *      |__   | -_|  _| | | -_|  _|   __| |   | . |   __| | | |_ -|
 *      |_____|___|_|  \_/|___|_| |__|  |_|_|_|_  |__|  |_|___|___|
 *                                            |___|
 *  ServerPingPlus - Customize your server ping!
 *  Copyright (C) 2014, Minecrell <https://github.com/Minecrell>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.minecrell.serverpingplus.core.config.yamlconf;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import net.minecrell.serverpingplus.core.util.Helper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class YAMLConf {
    public static final String COMMENT_PREFIX = "# ";
    public static final String DOCUMENT_START = "--- ";

    public static class SnakeYAML {
        private final @Getter @NonNull Yaml yaml;
        private final @Getter boolean outdated;

        private final @Getter @NonNull DumperOptions dumperOptions;
        private final @Getter @NonNull Constructor constructor;
        private final @Getter @NonNull Representer representer;

        public SnakeYAML(DumperOptions dumperOptions, Constructor constructor, Representer representer) {
            this(dumperOptions, constructor, representer, false);
        }

        public SnakeYAML(DumperOptions dumperOptions, Constructor constructor, Representer representer, boolean outdated) {
            this.yaml = new Yaml(constructor, representer, dumperOptions);
            this.dumperOptions = dumperOptions;
            this.constructor = constructor;
            this.representer = representer;
            this.outdated = outdated;
        }
    }

    private final @Getter @NonNull SnakeYAML snakeYAML;
    private final String newLine;
    private final Joiner commentWriter;

    public YAMLConf(SnakeYAML snakeYAML) {
        this.snakeYAML = Preconditions.checkNotNull(snakeYAML, "snakeYAML");
        this.newLine = snakeYAML.getDumperOptions().getLineBreak().getString();
        this.commentWriter = Joiner.on(newLine + COMMENT_PREFIX);
    }

    public String dump(Object conf) {
        return dump(null, conf);
    }

    public <T extends Writer> T save(T writer, Object conf) {
        return save(writer, null, conf);
    }

    public String dump(String[] header, Object conf) {
        return save(new StringWriter(), header, conf).toString();
    }

    @SuppressWarnings("deprecation")
    public <T extends Writer> T save(T writer, String[] header, Object conf) {
        try {
            if (!Helper.nullOrEmpty(header)) commentWriter.appendTo(writer, Iterators.forArray(header)).append(newLine);
            Tag root = snakeYAML.getDumperOptions().getExplicitRoot();
            snakeYAML.getDumperOptions().setExplicitRoot(Tag.MAP);
            snakeYAML.getYaml().dumpAll(Iterators.singletonIterator(conf), writer);
            snakeYAML.getDumperOptions().setExplicitRoot(root);
            return writer;
        } catch (IOException e) {
            throw new YAMLException(e);
        }
    }

    public String dumpAll(Object... confs) {
        return dumpAll(null, confs);
    }

    public <T extends Writer> T saveAll(T writer, Object... confs) {
        return saveAll(writer, null, confs);
    }

    public String dumpAll(String[] header, Object... confs) {
        return saveAll(new StringWriter(), header, confs).toString();
    }

    public <T extends Writer> T saveAll(T writer, String[] header, Object... confs) {
        try {
            if (!Helper.nullOrEmpty(header)) commentWriter.appendTo(writer, Iterators.forArray(header)).append(newLine);

            String[] description;
            for (Object conf : confs) {
                writer.append(newLine);
                description = ConfHelper.getDescription(conf);
                if (!Helper.nullOrEmpty(description)) commentWriter.appendTo(writer, Iterators.forArray(description));
                writer.append(DOCUMENT_START);
                snakeYAML.getYaml().dumpAll(Iterators.singletonIterator(conf), writer);
            }

            return writer;
        } catch (IOException e) {
            throw new YAMLException(e);
        }
    }
}