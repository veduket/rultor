/**
 * Copyright (c) 2009-2014, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.github.qtn;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Parametrized question.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.3.6
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnParametrized implements Question {

    /**
     * Pattern to find.
     */
    private static final Pattern PTN = Pattern.compile(
        "([a-zA-Z_]+)\\s*(?::|=|is)\\s*`([^`]+)`",
        Pattern.DOTALL | Pattern.MULTILINE
    );

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnParametrized(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment, final URI home) {
        return new Req() {
            @Override
            public Iterable<Directive> dirs() throws IOException {
                final Directives dirs = new Directives().append(
                    QnParametrized.this.origin.understand(comment, home).dirs()
                ).addIf("args");
                for (final Map.Entry<String, String> ent
                    : QnParametrized.params(comment).entrySet()) {
                    dirs.add("arg")
                        .attr("name", ent.getKey())
                        .set(ent.getValue())
                        .up();
                }
                return dirs.up();
            }
        };
    }

    /**
     * Fetch params from comment.
     * @param comment The comment
     * @return Map of params
     * @throws IOException If fails
     */
    private static Map<String, String> params(final Comment.Smart comment)
        throws IOException {
        final ImmutableMap.Builder<String, String> map =
            new ImmutableMap.Builder<String, String>();
        final Matcher matcher = QnParametrized.PTN.matcher(comment.body());
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map.build();
    }

}
