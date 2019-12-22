/**
 * (c) Jens Kübler
 * This software is public domain
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package busstop.customtrip.util;

import busstop.customtrip.model.FeaturesCount;
import nice.fontaine.overpass.models.response.geometries.Element;

/**
 *
 */

public final class OverpassParser {
    public static FeaturesCount parseHistoricToCount(Element[] elements) {
        int n = 0;
        int w = 0;
        int r = 0;

        // Monuments count
        n = Integer.parseInt(elements[0].tags.get("nodes"));
        w = Integer.parseInt(elements[0].tags.get("ways"));
        r = Integer.parseInt(elements[0].tags.get("relations"));

        FeaturesCount featuresCount = new FeaturesCount(n, w, r);

        return featuresCount;
    }

    public static FeaturesCount parseGreenToCount(Element[] elements) {
        int n = 0;
        int w = 0;
        int r = 0;

        // Green ares count
        n = Integer.parseInt(elements[1].tags.get("nodes"));
        w = Integer.parseInt(elements[1].tags.get("ways"));
        r = Integer.parseInt(elements[1].tags.get("relations"));

        FeaturesCount featuresCount = new FeaturesCount(n, w, r);

        return featuresCount;
    }

    public static FeaturesCount parsePanoramicToCount(Element[] elements) {
        int n = 0;
        int w = 0;
        int r = 0;

        // Panoramic count
        n = Integer.parseInt(elements[2].tags.get("nodes"));
        w = Integer.parseInt(elements[2].tags.get("ways"));
        r = Integer.parseInt(elements[2].tags.get("relations"));

        FeaturesCount featuresCount = new FeaturesCount(n, w, r);

        return featuresCount;
    }

//    public static Features parseFeaturesToFeatures(Element[] elements) {
//
//    }
}