/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.desktop.common.support;

import haveno.desktop.common.view.AbstractView;
import haveno.desktop.common.view.CachingViewLoader;
import haveno.desktop.common.view.ViewLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class CachingViewLoaderTests {

    private ViewLoader delegateViewLoader;
    private ViewLoader cachingViewLoader;

    @BeforeEach
    public void setUp() {
        delegateViewLoader = mock(ViewLoader.class);
        cachingViewLoader = new CachingViewLoader(delegateViewLoader);

        // Configure the mock to return valid View objects
        when(delegateViewLoader.load(TestView1.class)).thenReturn(new TestView1());
        when(delegateViewLoader.load(TestView2.class)).thenReturn(new TestView2());
    }

    @Test
    public void test() {
        cachingViewLoader.load(TestView1.class);
        cachingViewLoader.load(TestView1.class);
        cachingViewLoader.load(TestView2.class);

        then(delegateViewLoader).should(times(1)).load(TestView1.class);
        then(delegateViewLoader).should(times(1)).load(TestView2.class);
        then(delegateViewLoader).should(times(0)).load(TestView3.class);
    }

    static class TestView1 extends AbstractView {
    }

    static class TestView2 extends AbstractView {
    }

    static class TestView3 extends AbstractView {
    }
}
    static class TestView3 extends AbstractView {
    }
}
