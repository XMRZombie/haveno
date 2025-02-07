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

package haveno.desktop.common.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class CachingViewLoader implements ViewLoader {

    private final ConcurrentMap<Class<? extends View>, View> cache = new ConcurrentHashMap<>();
    private final ViewLoader viewLoader;

    @Inject
    public CachingViewLoader(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    @Override
    public View load(Class<? extends View> viewClass) {
        // Attempt to retrieve the view from the cache
        View cachedView = cache.get(viewClass);
        if (cachedView != null) {
            return cachedView; // Return cached view if present
        }
        // Load the view if not in cache
        View view = viewLoader.load(viewClass);
        // Cache the loaded view only if it was not already cached
        View existingView = cache.putIfAbsent(viewClass, view);
        return existingView != null ? existingView : view; // Return the cached view if it was added by another thread
    }

    public void removeFromCache(Class<? extends View> viewClass) {
        cache.remove(viewClass);
    }
}

