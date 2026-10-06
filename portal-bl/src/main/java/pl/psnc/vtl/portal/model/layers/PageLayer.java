package pl.psnc.vtl.portal.model.layers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.layers.CustomLayerSerializer;
import pl.psnc.vtl.portal.services.layers.CustomPageSerializer;
import pl.psnc.vtl.portal.services.layers.CustomVerseListSerializer;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Page - Layer connection.
 */
@Entity
@Table(name = "page_layers")
@NoArgsConstructor
@Getter
@Setter
public class PageLayer {

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Serializable {

        /**
         * Page id.
         */
        private Long pageId;

        /**
         * Layer id.
         */
        private Long layerId;
    }

    @EmbeddedId
    @JsonIgnore
    private Id id = new Id();

    /**
     * Page that this page layer exists on.
     */
    @MapsId("pageId")
    @ManyToOne
    @JoinColumn(name = "page_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonSerialize(using = CustomPageSerializer.class)
    private Page page;

    /**
     * Layer that this page layer exists on.
     */
    @MapsId("layerId")
    @ManyToOne
    @JoinColumn(name = "layer_id", insertable = false, updatable = false)
    @JsonSerialize(using = CustomLayerSerializer.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OrderColumn(name = "id")
    private Layer layer;

    /**
     * Percent of transcribed verses on this page layer.
     */
    @Basic
    @Column
    @Nullable
    private Integer percentOfTranscribed;

    /**
     * Percent of verified verses on this page layer.
     */
    @Basic
    @Column
    @JsonIgnore
    @Nullable
    private Integer percentOfVerified;

    /**
     * Creation date of this page layer.
     */
    @Column(updatable = false)
    @NotNull
    private Date addedOn = new Date();

    /**
     * List of verses that exists on this page layer.
     */
    @OneToMany(mappedBy = "pageLayer", cascade = {CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE})
    @Nullable
    @JsonSerialize(using = CustomVerseListSerializer.class)
    private List<Verse> verses = new ArrayList<>();


    public PageLayer(Page page, Layer layer) {
        this.page = page;
        this.layer = layer;
        this.id.pageId = page.getId();
        this.id.layerId = layer.getId();
        page.getPageLayers().add(this);
        layer.getLayerPages().add(this);
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
        this.id.pageId = page.getId();
        page.getPageLayers().add(this);
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
        this.id.layerId = layer.getId();
        layer.getLayerPages().add(this);
    }

    /**
     * Name of user that lastly modified content on this page layer.
     */
    private String lastModifiedBy;

    /**
     * Page layer last modification date.
     */
    private LocalDateTime lastModificationDate;

    @Transient
    private List<Object> blocks;
}
